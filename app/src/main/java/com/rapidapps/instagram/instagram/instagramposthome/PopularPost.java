package com.rapidapps.instagram.instagram.instagramposthome;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.rapidapps.instagram.instagram.R;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link PopularPost.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link PopularPost#newInstance} factory method to
 * create an instance of this fragment.
 */
public class PopularPost extends Fragment {

    public PopularPost() {
        // Required empty public constructor
    }


    public static PopularPost newInstance() {

        PopularPost fragment = new PopularPost();
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_popular_post, container, false);
    }

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
