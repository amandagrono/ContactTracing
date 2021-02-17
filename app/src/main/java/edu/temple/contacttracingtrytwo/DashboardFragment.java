package edu.temple.contacttracingtrytwo;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;




public class DashboardFragment extends Fragment {
    private static final String LISTENER_KEY = "click_listener";
    private DashboardButtonListener listener = null;

    public DashboardFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment MainPage.
     */
    public static DashboardFragment newInstance() {
        return new DashboardFragment();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        if (context instanceof DashboardButtonListener) {
            this.listener = (DashboardButtonListener) context;
        } else {
            throw new RuntimeException(context.toString() + " must implement OnClickTrackingButtonListener");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_dashboard, container, false);
        root.findViewById(R.id.startButton).setOnClickListener(view -> listener.onStartTracking());
        root.findViewById(R.id.stopButton).setOnClickListener(view -> listener.onStopTracking());
        return root;
    }




}