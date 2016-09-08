package com.ionicframework.udubsit252887.ui;


import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.ionicframework.udubsit252887.R;
import com.ionicframework.udubsit252887.Utils.Constants;
import com.ionicframework.udubsit252887.Utils.Utils;
import com.ionicframework.udubsit252887.models.Schedual;

/**
 * A simple {@link Fragment} subclass.
 */
public class SchedualFragment extends Fragment implements SearchView.OnQueryTextListener {


    private View rootView;
    private RecyclerView mSchedualRecycler;
    private FirebaseRecyclerAdapter<Schedual, SchedualHolder> mSchedualAdapter;
    private SearchView mSearchView;
    private ImageView filterView;
    private SharedPreferences sp;
    private String limitQueryStr;
    private Query databaseRef;

    public SchedualFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        rootView = inflater.inflate(R.layout.fragment_schedual, container, false);

        initialiseScreen();

        return rootView;
    }

    private void initialiseScreen() {
        mSchedualRecycler = (RecyclerView) rootView.findViewById(R.id.schedual_recycler);
        mSearchView = (SearchView) rootView.findViewById(R.id.search_view);
        mSearchView.setOnQueryTextListener(this);
    }

    @Override
    public void onStart() {
        super.onStart();
        setupAdapter();
    }

    private void setupAdapter() {
        if (limitQueryStr != null) {
            databaseRef = FirebaseDatabase.getInstance().getReference(Constants.SCHEDULE_KEY)
                    .orderByChild(sp.getString(Constants.SCHEDULE_FILTER, "contact"))
                    .startAt(limitQueryStr)
                    .endAt(limitQueryStr + "~");
        } else {
            databaseRef = FirebaseDatabase.getInstance().getReference(Constants.SCHEDULE_KEY);
        }

        mSchedualAdapter = new FirebaseRecyclerAdapter<Schedual, SchedualHolder>(
                Schedual.class,
                R.layout.item_layout_schedual,
                SchedualHolder.class,
                databaseRef
        ) {
            @Override
            protected void populateViewHolder(SchedualHolder viewHolder, Schedual model, int position) {
                viewHolder.setContact(model.getContact());
                viewHolder.setRoom(model.getRoom());
                viewHolder.setTitle(model.getTitle());
            }
        };
        LinearLayoutManager manager = new LinearLayoutManager(getContext());
        mSchedualRecycler.setLayoutManager(manager);
        mSchedualRecycler.setAdapter(mSchedualAdapter);
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        if (query.length() == 0) {
            limitQueryStr = null;
        } else {
            limitQueryStr = query.toUpperCase();
        }
        setupAdapter();
        return true;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        return false;
    }

    public static class SchedualHolder extends RecyclerView.ViewHolder {

        TextView schedualContact;
        TextView schedualRoom;
        TextView schedualTitle;
        TextView startDateText;
        TextView startTimeText;
        TextView endDateText;
        TextView endTimeText;

        public SchedualHolder(View itemView) {
            super(itemView);

            schedualContact = (TextView) itemView.findViewById(R.id.schedual_contact);
            schedualRoom = (TextView) itemView.findViewById(R.id.schedual_room);
            schedualTitle = (TextView) itemView.findViewById(R.id.schedual_title);
            startDateText = (TextView) itemView.findViewById(R.id.start_date_text);
            startTimeText = (TextView) itemView.findViewById(R.id.start_time_text);
            endDateText = (TextView) itemView.findViewById(R.id.end_date_text);
            endTimeText = (TextView) itemView.findViewById(R.id.end_time_text);
        }

        public void setContact(String contact) {
            schedualContact.setText(Utils.getCaseSensitiveText(contact));
        }

        public void setRoom(String room) {
            schedualRoom.setText(room);
        }

        public void setTitle(String title) {
            schedualTitle.setText(title);
        }
    }
}