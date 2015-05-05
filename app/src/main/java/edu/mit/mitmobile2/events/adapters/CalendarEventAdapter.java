package edu.mit.mitmobile2.events.adapters;

import android.content.Context;
import android.text.format.DateFormat;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

import edu.mit.mitmobile2.R;
import edu.mit.mitmobile2.events.model.MITCalendarEvent;
import edu.mit.mitmobile2.events.model.MITCalendarLocation;

public class CalendarEventAdapter extends BaseAdapter {

    private class ViewHolder {
        TextView eventTitle;
        TextView eventLocation;
        TextView eventTime;
    }

    private List<MITCalendarEvent> events;
    private Context context;

    public CalendarEventAdapter(Context context, List<MITCalendarEvent> events) {
        this.context = context;
        this.events = events;
    }

    @Override
    public int getCount() {
        return events.size();
    }

    @Override
    public Object getItem(int position) {
        return events.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        ViewHolder holder;

        if (view == null) {
            holder = new ViewHolder();
            view = View.inflate(context, R.layout.events_list_row, null);
            holder.eventLocation = (TextView) view.findViewById(R.id.event_location);
            holder.eventTitle = (TextView) view.findViewById(R.id.event_title);
            holder.eventTime = (TextView) view.findViewById(R.id.event_time);

            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }

        MITCalendarEvent event = (MITCalendarEvent) getItem(position);
        holder.eventTitle.setText(event.getTitle());

        MITCalendarLocation location = event.getLocation();
        if (location != null) {
            holder.eventLocation.setText(location.getRoomNumber());
        }

        holder.eventTime.setText(DateFormat.format("h:mm a", event.getStartDate()) + " - " + DateFormat.format("h:mm a", event.getEndDate()));

        return view;

    }

    public void updateEvents(List<MITCalendarEvent> events) {
        this.events = events;
        notifyDataSetChanged();
    }
}