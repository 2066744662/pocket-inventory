package com.example.pocketinventory;


import android.content.Context;
import android.content.Intent;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.Log;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * This class is the adapter for the recycler view. It takes in a list of items and displays them
 * in the recycler view.
 */
public class ItemAdapter extends RecyclerView.Adapter<ItemAdapter.ViewHolder> {

    private List<Item> data;
    private Context context;
    private SelectionViewModel selectionViewModel;
    private boolean isEnable = false;
    private boolean isSelectAll = false;
    private ArrayList<Item> selectItems = new ArrayList<>();

    /**
     * Constructor for the adapter
     * @param context
     * @param data
     */
    public ItemAdapter(Context context, List<Item> data) {
        this.context = context;
        this.data = data;
    }

    /**
     * This method inflates the view and returns a ViewHolder object
     * @param parent The ViewGroup into which the new View will be added after it is bound to
     *               an adapter position.
     * @param viewType The view type of the new View.
     *
     * @return ViewHolder object
     */
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_list_fragment, parent, false);
        ViewModelProvider viewModelProvider = new ViewModelProvider((FragmentActivity) context);
        selectionViewModel = viewModelProvider.get(SelectionViewModel.class);
        return new ViewHolder(view);
    }

    /**
     * This method binds the data to the view holder
     *
     * This method is called by the RecyclerView to display data at the specified position.
     * It binds the data item to the provided ViewHolder, sets up long click and click listeners,
     * and manages the selection state for items when the user interacts with the view.
     *
     * Citation: https://youtu.be/Uld0N4ofgWQ?si=5ZYiswWMMzLF1FcL
     *
     * @param holder The ViewHolder which should be updated to represent the contents of the
     *        item at the given position in the data set.
     * @param position The position of the item within the adapter's data set.
     */
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        // Obtain the data item for the specified position
        Item item = data.get(position);
        // Bind the data item to the ViewHolder, updating the view
        holder.bind(item);

        // Set up a long click listener to initiate ActionMode for item selection
        // Reference: https://youtu.be/Uld0N4ofgWQ?si=5ZYiswWMMzLF1FcL
        // Used the above resource's code structure to compelete the functionality below:
        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                // First instance after long press: ActionMode not enabled yet
                // Allows to create ActionMode before using it
                if (!isEnable){
                    // Create ActionMode callback
                    ActionMode.Callback callback = new ActionMode.Callback() {

                        @Override // Override the method to create an Action Mode, which is a contextual action bar.
                        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                            // This method is called when the action mode is being created.

                            // Inflate a menu layout
                            MenuInflater menuInflater = mode.getMenuInflater();
                            menuInflater.inflate(R.menu.menu, menu);
                            // The code above inflates (loads) a menu layout resource named "menu" into the provided Menu object.

                            return true;
                            // Return true to indicate that the Action Mode has been created successfully.
                        }

                        @Override // Override the method to prepare the Action Mode for display.
                        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                            // This method is called before the Action Mode is displayed to the user.

                            // Set the "isEnable" flag to true because Action Mode needs to be enabled for its contents to be displayed
                            isEnable = true;

                            // Call the ClickItem method on the holder object
                            ClickItem(holder);

                            // Observe changes in the text value from the selectionViewModel (tracks the changes in number of selected items)
                            selectionViewModel.getText().observe((LifecycleOwner) context, new Observer<String>() {
                                @Override
                                public void onChanged(String s) {
                                    // When the text value changes, update the Action Mode's title (i.e., number of selected items)
                                    mode.setTitle(String.format("%s Selected", s));
                                }
                            });

                            return true;
                            // Return true to indicate that the Action Mode has been prepared successfully.
                        }



                        @Override // Override the method to handle user clicks on action items within the Action Mode.
                        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                            // This method is called when an action item is clicked.

                            // Get the ID of the clicked menu item
                            int menuItem = item.getItemId();

                            // Check if the clicked item's ID matches the "delete_icon" defined in menu.xml (R.id.delete_icon)
                            if (menuItem == R.id.delete_icon){

                                // Iterate through the selectItems list (assuming it contains selected items)
                                for (Item item1 : selectItems){
                                    // Remove each selected item from itemDB
                                    ItemDB.getInstance().deleteItem(item1);
                                }
                                // Update Item Data in HomePageActivity as well
                                ((HomePageActivity)context).updateItemData();

                                // Finish the Action Mode, exiting the contextual action bar
                                mode.finish();
                            }
                            // Need to implement the add_tag_icon in the future

                            /*
                            if (menuItem == R.id.add_tag_icon){

                            }
                            */

                            if (menuItem == R.id.select_all_icon){
                                // Check if the clicked item's ID matches the "select_all_icon" defined in menu.xml (R.id.select_all_icon)
                                if (selectItems.size() == data.size()){
                                    // If the number of selected items is equal to the total number of items in the data:

                                    // No need for selectAll as all items manually are selected
                                    isSelectAll = false;

                                    // Clear the list of selected items ("selectItems")
                                    selectItems.clear();
                                }
                                else{// If not all items are selected:

                                    // There is need for selectAll functionality
                                    isSelectAll = true;

                                    // Add all the items from the data to selectItems without duplication
                                    selectItems.clear();
                                    selectItems.addAll(data);
                                }

                                // Update the text in the selectionViewModel to display the count of selected items
                                selectionViewModel.setText(String.valueOf(selectItems.size()));

                                // Notify the adapter to refresh the UI, reflecting the changes in selection
                                notifyDataSetChanged();
                            }

                            return true; // Return true to indicate that the Action Mode has been prepared successfully.
                        }

                        @Override // Override the method to handle Finish Action Mode.
                        public void onDestroyActionMode(ActionMode mode) {
                            // This method is called when mode is set to finish ("mode.finish()")

                            // Reset the Boolean flags
                            isEnable = false;
                            isSelectAll = false;

                            // Reset the selected items list ("selectItems")
                            selectItems.clear();

                            // Notify the adapter to refresh the UI
                            notifyDataSetChanged();

                        }
                    };

                    // Call the ActionMode
                    ((AppCompatActivity) v.getContext()).startActionMode(callback);

                }
                else { // If ActionMode already enabled

                    // Handle click on Items through the ClickItem() function which shows the checkboxes
                    ClickItem(holder);
                }

                return true; // Return true to indicate that the .setOnLongPress() has been prepared successfully
            }
        });

        // Set up a click listener to toggle item selection when not in ActionMode
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isEnable){
                    // Handle item selection when ActionMode is enabled
                    ClickItem(holder);
                } else {
                    // Go to the item details page when ActionMode is not enabled
                    Intent intent = new Intent(context, ItemAddActivity.class);
                    intent.putExtra("item", item);
                    context.startActivity(intent);
                }
            }
        });

        // Update the checkmark icon visibility based on selection state: isSelectAll or Not isSelectAll
        if (isSelectAll){
            holder.checkedBoxImageView.setVisibility(View.VISIBLE);
        }
        else{
            holder.checkedBoxImageView.setVisibility(View.GONE);
        }
    }

    /**
     * This method returns the number of items in the data set held by the adapter.
     * @return number of items in the data set
     */
    @Override
    public int getItemCount() {
        return data.size();
    }
    /**
     * This method returns the list of items
     * @return list of items
     */
    public List<Item> getList() {
        return data;
    }

    /**
     * This method updates the total value text view, and call notifyDataSetChanged()
     * Call this instead of notifyDataSetChanged() :)
     */
    public void update(){
        double total = 0;
        for (Item item : data) {
            total += item.getValue();
        }
        TextView totalValueText = ((HomePageActivity)context).findViewById(R.id.total_value_text);
        totalValueText.setText(String.format("$ %.2f", total));
        notifyDataSetChanged();
    }

    /**
     * This method handles the click event for an item in the RecyclerView.
     *
     * This method toggles the selection state of the item represented by the given ViewHolder.
     * When the item is selected, it displays a checkmark icon, and when deselected, it hides the icon.
     * The method also updates a count of selected items and communicates the count to a ViewModel.
     *
     * Citation: https://youtu.be/Uld0N4ofgWQ?si=5ZYiswWMMzLF1FcL
     *
     * @param holder The ViewHolder associated with the clicked item.
     */
    private void ClickItem(ViewHolder holder) {
        // Get the selected item from the data list based on the adapter position
        Item itemSelected = data.get(holder.getAdapterPosition());

        // Toggle the visibility of the checkmark icon based on the current state
        if (holder.checkedBoxImageView.getVisibility() == View.GONE) {
            holder.checkedBoxImageView.setVisibility(View.VISIBLE);
            selectItems.add(itemSelected);
        } else {
            holder.checkedBoxImageView.setVisibility(View.GONE);
            selectItems.remove(itemSelected);
        }

        // Update the count of selected items and communicate it to a ViewModel
        selectionViewModel.setText(String.valueOf(selectItems.size()));
    }

    /**
     * This class is the view holder for the recycler view. It holds the views that will be
     * displayed in the recycler view.
     */
    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView dateTextView;
        private TextView makeTextView;
        private TextView modelTextView;
        private TextView descriptionTextView;
        private TextView valueTextView;
        private TextView commentTextView;
        private ImageView checkedBoxImageView;
        private LinearLayout tagsLinearLayout;
        private Chip tag1Chip;
        private Chip tag2Chip;
        private Chip tag3Chip;
        private Chip tag4Chip;
        private Chip tag5Chip;
        private ArrayList<String> tagsList;
        private RecyclerView recyclerView;


        /**
         * Constructor for the view holder
         * @param itemView
         */
        public ViewHolder(View itemView) {
            super(itemView);
            dateTextView = itemView.findViewById(R.id.dateTextView);
            makeTextView = itemView.findViewById(R.id.makeTextView);
            modelTextView = itemView.findViewById(R.id.modelTextView);
            descriptionTextView = itemView.findViewById(R.id.descriptionTextView);
            valueTextView = itemView.findViewById(R.id.valueTextView);
            commentTextView = itemView.findViewById(R.id.commentTextView);
            checkedBoxImageView = itemView.findViewById(R.id.checkImageView);
            tagsLinearLayout = itemView.findViewById(R.id.tagsLinearLayout);
            tag1Chip = itemView.findViewById(R.id.tag1Chip);
            tag2Chip = itemView.findViewById(R.id.tag2Chip);
            tag3Chip = itemView.findViewById(R.id.tag3Chip);
            tag4Chip = itemView.findViewById(R.id.tag4Chip);
            tag5Chip = itemView.findViewById(R.id.tag5Chip);
            recyclerView = itemView.findViewById(R.id.tagList);
        }

        /**
         * This method binds the data to the view holder
         * @param item
         */
        public void bind(Item item) {
            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            dateTextView.setText("Date: " + dateFormat.format(item.getDate()));
            makeTextView.setText("Make: " + item.getMake());
            modelTextView.setText("Model: " + item.getModel());
            descriptionTextView.setText("Description: " + item.getDescription());
            valueTextView.setText("Value: $" + item.getValue());
            commentTextView.setText("Comment: " + item.getComment());
            tagsList = new ArrayList<>();

            // Iterate through the list of tags in the 'item' object and add them to the 'tagsList' collection.
            for (String tag : item.getTags()) {
                tagsList.add(tag);
            }

            // If there are tags in the 'tagsList' collection:
            if (tagsList.size() > 0) {
                // Make the 'tagsLinearLayout' visible.
                tagsLinearLayout.setVisibility(View.VISIBLE);

                // Loop through the tags in 'tagsList' and display them in corresponding Chip views.
                for (int i = 0; i < tagsList.size(); i++) {
                    if (i == 0) {
                        // Display and set text for 'tag1Chip'.
                        tag1Chip.setVisibility(View.VISIBLE);
                        tag1Chip.setText(tagsList.get(i));
                    }
                    if (i == 1) {
                        // Display and set text for 'tag2Chip'.
                        tag2Chip.setVisibility(View.VISIBLE);
                        tag2Chip.setText(tagsList.get(i));
                    }
                    if (i == 2) {
                        // Display and set text for 'tag3Chip'.
                        tag3Chip.setVisibility(View.VISIBLE);
                        tag3Chip.setText(tagsList.get(i));
                    }
                    if (i == 3) {
                        // Display and set text for 'tag4Chip'.
                        tag4Chip.setVisibility(View.VISIBLE);
                        tag4Chip.setText(tagsList.get(i));
                    }
                    if (i == 4) {
                        // Display and set text for 'tag5Chip'.
                        tag5Chip.setVisibility(View.VISIBLE);
                        tag5Chip.setText(tagsList.get(i));
                    }
                }
            }

            // Set up a child recyclerView to display the tags in a horizontal list.
            recyclerView.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false));
            tagAdapter adapter = new tagAdapter(context, tagsList);
            recyclerView.setAdapter(adapter);
            /*

            Need to implement this eventually for the +More to work in the future

            if (tagsList.size() > 0){
                tagsLinearLayout.setVisibility(View.VISIBLE);
                for (int i =0; i < tagsList.size(); i++){
                    if (i == 0){
                        tag1Chip.setVisibility(View.VISIBLE);
                        tag1Chip.setText(tagsList.get(i));
                    }
                    if (i == 1){
                        tag2Chip.setVisibility(View.VISIBLE);
                        tag2Chip.setText(tagsList.get(i));
                        if (checkOutOfWindow(tag2Chip)) {
                            tag2Chip.setText("+" + String.valueOf(tagsList.size() - i));
                            break;
                        }

                    }
                    if (i == 2){
                        tag3Chip.setVisibility(View.VISIBLE);
                        tag3Chip.setText(tagsList.get(i));
                        if (checkOutOfWindow(tag3Chip)) {
                            tag3Chip.setText("+" + String.valueOf(tagsList.size() - i));
                            break;
                        }
                    }
                    if (i == 3){
                        tag4Chip.setVisibility(View.VISIBLE);
                        tag4Chip.setText(tagsList.get(i));
                        if (checkOutOfWindow(tag4Chip)) {
                            tag4Chip.setText("+" + String.valueOf(tagsList.size() - i));
                            break;
                        }
                    }
                    if (i == 4){
                        tag5Chip.setVisibility(View.VISIBLE);
                        tag5Chip.setText(tagsList.get(i));
                        if (checkOutOfWindow(tag5Chip)) {
                            tag5Chip.setText("+" + String.valueOf(tagsList.size() - i));
                            break;
                        }
                    }
                }

            }*/

        }

        /**
         * Checks if a Chip is partially or fully out of the visible window within its parent view.
         *
         * @param tagChip The Chip to be checked for visibility within the window.
         * @return {@code true} if the Chip is partially or fully out of the visible window, {@code false} otherwise.
         */
        private Boolean checkOutOfWindow(Chip tagChip) {
            // Get the location of the Chip on the screen
            int[] chipLocation = new int[2];
            tagChip.getLocationOnScreen(chipLocation);
            int tagX = chipLocation[0];

            // Get the parent view where the Chip is placed (e.g., a LinearLayout)
            View parentView = tagsLinearLayout;

            // Get the width of the parent view, which represents the visible window
            int windowWidth = parentView.getWidth();

            // Get the text and paint information from the Chip
            String tagText = tagChip.getText().toString();
            Paint tagPaint = tagChip.getPaint();

            // Calculate the width of the Chip's text and the width of the Chip itself
            float textWidth = tagPaint.measureText(tagText);
            float chipWidth = tagChip.getWidth();

            // Check if the Chip's text extends beyond the right edge of the visible window
            if (tagX + textWidth > windowWidth) {
                // The text within the Chip is not fully visible within the window
                // You can handle this condition here, e.g., by resizing the text, changing the Chip's size, or showing a message
                return Boolean.TRUE;
            } else {
                // The Chip's text is fully visible within the window
                return Boolean.FALSE;
            }
        }


    }
}

