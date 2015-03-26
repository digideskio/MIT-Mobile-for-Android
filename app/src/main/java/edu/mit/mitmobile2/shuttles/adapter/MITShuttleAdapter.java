package edu.mit.mitmobile2.shuttles.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import edu.mit.mitmobile2.R;
import edu.mit.mitmobile2.shuttles.ShuttleAdapterCallback;
import edu.mit.mitmobile2.shuttles.ShuttleUtils;
import edu.mit.mitmobile2.shuttles.model.MITShuttleRoute;
import edu.mit.mitmobile2.shuttles.model.MITShuttleStopWrapper;
import edu.mit.mitmobile2.shuttles.model.MitMiniShuttleRoute;


public class MITShuttleAdapter extends BaseAdapter {

    private LayoutInflater listContainer;
    private List<MitMiniShuttleRoute> routes = new ArrayList<>();
    private Context context;
    private ShuttleAdapterCallback callback;

    public MITShuttleAdapter(Context context, List<MitMiniShuttleRoute> routes, ShuttleAdapterCallback callback) {
        listContainer = LayoutInflater.from(context);
        this.routes = routes;
        this.context = context;
        this.callback = callback;
    }

    @Override
    public int getCount() {
        return routes.size();
    }

    @Override
    public Object getItem(int position) {
        return routes.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View view, ViewGroup parent) {
        ViewHolder viewHolder;
        if (view != null) {
            viewHolder = (ViewHolder) view.getTag();
        } else {
            view = listContainer.inflate(R.layout.shuttle_list_item, parent, false);
            viewHolder = new ViewHolder(view);
            view.setTag(viewHolder);
        }

        if (routes.get(position).isPredictable()) {
            viewHolder.shuttleRouteImageView.setImageResource(R.drawable.shuttle_big_active);
            viewHolder.shuttleStopView.setVisibility(View.VISIBLE);
        } else if (routes.get(position).isScheduled()) {
            viewHolder.shuttleRouteImageView.setImageResource(R.drawable.shuttle_big_unknown);
            viewHolder.shuttleStopView.setVisibility(View.GONE);
        } else {
            viewHolder.shuttleRouteImageView.setImageResource(R.drawable.shuttle_big_inactive);
            viewHolder.shuttleStopView.setVisibility(View.GONE);
        }

        viewHolder.shuttleRouteTextview.setText(routes.get(position).getTitle());
        if (routes.get(position).isPredictable()) {
            initialViewVisibility(viewHolder, View.VISIBLE);
            MITShuttleStopWrapper stop1 = routes.get(position).getStops().get(0);
            MITShuttleStopWrapper stop2 = routes.get(position).getStops().get(1);

            viewHolder.firstStopTextView.setText(stop1.getTitle());
            viewHolder.secondStopTextView.setText(stop2.getTitle());

            viewHolder.firstStopMinuteTextView.setTextColor(context.getResources().getColor(R.color.contents_text));
            viewHolder.firstStopMinuteTextView.setText(ShuttleUtils.formatPredictionFromStop(stop1));
            if (viewHolder.firstStopMinuteTextView.getText().toString().equals(ShuttleUtils.NOW)) {
                viewHolder.firstStopMinuteTextView.setTextColor(context.getResources().getColor(R.color.mit_tintColor));
            }

            viewHolder.secondStopMinuteTextView.setTextColor(context.getResources().getColor(R.color.contents_text));
            viewHolder.secondStopMinuteTextView.setText(ShuttleUtils.formatPredictionFromStop(stop2));
            if (viewHolder.secondStopMinuteTextView.getText().toString().equals(ShuttleUtils.NOW)) {
                viewHolder.secondStopMinuteTextView.setTextColor(context.getResources().getColor(R.color.mit_tintColor));
            }
        } else {
            initialViewVisibility(viewHolder, View.GONE);
        }

        viewHolder.shuttleRoute.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callback.shuttleRouteClick(routes.get(position).getId());
            }
        });
        viewHolder.shuttleFirstStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callback.shuttleStopClick(routes.get(position).getId(), routes.get(position).getStops().get(0).getId());
            }
        });
        viewHolder.shuttleSecondStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callback.shuttleStopClick(routes.get(position).getId(), routes.get(position).getStops().get(1).getId());
            }
        });

        return view;
    }

    public void initialViewVisibility(ViewHolder viewHolder, int view) {
        viewHolder.firstStopTextView.setVisibility(view);
        viewHolder.firstStopMinuteTextView.setVisibility(view);
        viewHolder.secondStopTextView.setVisibility(view);
        viewHolder.secondStopMinuteTextView.setVisibility(view);
        viewHolder.topView.setVisibility(view);
        viewHolder.bottomView.setVisibility(view);
    }

    static class ViewHolder {
        @InjectView(R.id.shuttle_route_imageview)
        ImageView shuttleRouteImageView;

        @InjectView(R.id.shuttle_route_textview)
        TextView shuttleRouteTextview;

        @InjectView(R.id.first_stop_textview)
        TextView firstStopTextView;

        @InjectView(R.id.first_minute_textview)
        TextView firstStopMinuteTextView;

        @InjectView(R.id.second_stop_textview)
        TextView secondStopTextView;

        @InjectView(R.id.second_minute_textview)
        TextView secondStopMinuteTextView;

        @InjectView(R.id.top_view)
        View topView;

        @InjectView(R.id.bottom_view)
        View bottomView;

        @InjectView(R.id.shuttle_route)
        RelativeLayout shuttleRoute;

        @InjectView(R.id.shuttle_route_first_stop)
        RelativeLayout shuttleFirstStop;

        @InjectView(R.id.shuttle_route_second_stop)
        RelativeLayout shuttleSecondStop;

        @InjectView(R.id.shuttle_stop_view)
        LinearLayout shuttleStopView;

        ViewHolder(View view) {
            ButterKnife.inject(this, view);
        }
    }

    public void updateListItems(List<MitMiniShuttleRoute> routes) {
        this.routes = routes;
        notifyDataSetChanged();
    }

    public String getRouteStopTuples(String agency) {
        StringBuilder sb = new StringBuilder();
        for (MITShuttleRoute route : routes) {
            if (route.isPredictable() && route.getAgency().equals(agency)) {
                MITShuttleStopWrapper stop1 = route.getStops().get(0);
                MITShuttleStopWrapper stop2 = route.getStops().get(1);

                appendTuples(sb, route, stop1);
                appendTuples(sb, route, stop2);
            }
        }
        if (sb.length() > 0) {
            sb.deleteCharAt(sb.length() - 1);
        }
        return sb.toString();
    }

    private void appendTuples(StringBuilder sb, MITShuttleRoute route, MITShuttleStopWrapper stop1) {
        sb.append(route.getId());
        sb.append(",");
        sb.append(stop1.getId());
        sb.append(";");
    }
}