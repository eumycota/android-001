package course.labs.todomanager;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.RelativeLayout;
import android.widget.TextView;
import course.labs.todomanager.ToDoItem.Status;

public class ToDoListAdapter extends BaseAdapter {

	// List of ToDoItems
	private final List<ToDoItem> mItems = new ArrayList<ToDoItem>();
	
	private final Context mContext;

	private static final String TAG = "Lab-UserInterface";

	public ToDoListAdapter(Context context) {

		mContext = context;

	}

	// Add a ToDoItem to the adapter
	// Notify observers that the data set has changed

	public void add(ToDoItem item) {

		mItems.add(item);
		notifyDataSetChanged();

	}
	
	// Clears the list adapter of all items.
	
	public void clear(){

		mItems.clear();
		notifyDataSetChanged();
	
	}

	// Returns the number of ToDoItems

	@Override
	public int getCount() {

		return mItems.size();

	}

	// Retrieve the number of ToDoItems

	@Override
	public Object getItem(int pos) {

		return mItems.get(pos);

	}

	// Get the ID for the ToDoItem
	// In this case it's just the position

	@Override
	public long getItemId(int pos) {

		return pos;

	}

	//Create a View to display the ToDoItem 
	// at specified position in mItems

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {


		//TODO1 - Get the current ToDoItem
		final ToDoItem toDoItem = mItems.get(position);

		//TODO1 - Inflate the View for this ToDoItem
		// from todo_item.xml.
        LayoutInflater inflater = LayoutInflater.from(mContext);
		RelativeLayout itemLayout = (RelativeLayout) inflater.inflate(R.layout.todo_item, null);
		
		//TODO1 - Fill in specific ToDoItem data
		// Remember that the data that goes in this View
		// corresponds to the user interface elements defined 
		// in the layout file 

		//TODO1 - Display Title in TextView

        assert itemLayout != null;
        final TextView titleView = (TextView) itemLayout.findViewById(R.id.titleView);
        titleView.setText(toDoItem.getTitle());
		
		// TODO1 - Set up Status CheckBox
	
		final CheckBox statusView = (CheckBox) itemLayout.findViewById(R.id.statusCheckBox);
        statusView.setChecked( toDoItem.getStatus() == Status.DONE );
		
		
		statusView.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				log("Entered onCheckedChanged()");
				
				// TODO1 - Set up and implement an OnCheckedChangeListener, which
				// is called when the user toggles the status checkbox
                Status status = isChecked ? Status.DONE : Status.NOTDONE;
                toDoItem.setStatus(status);

			
			}
		});

		//TODO1 - Display Priority in a TextView

		final TextView priorityView = (TextView) itemLayout.findViewById(R.id.priorityView);
        priorityView.setText( toDoItem.getPriority().toString() );

		// TODO1 - Display Time and Date.
		// Hint - use ToDoItem.FORMAT.format(toDoItem.getDate()) to get date and time String

		final TextView dateView = (TextView) itemLayout.findViewById(R.id.dateView);
        dateView.setText( ToDoItem.FORMAT.format( toDoItem.getDate() ) );


		// Return the View you just created
		return itemLayout;

	}
	
	private void log(String msg) {
		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		Log.i(TAG, msg);
	}

}
