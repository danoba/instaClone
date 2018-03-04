package com.rapidapps.instagram.instagram.instagramposthome;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.alexzh.circleimageview.CircleImageView;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.rapidapps.instagram.instagram.MainActivity;
import com.rapidapps.instagram.instagram.R;
import com.rapidapps.instagram.instagram.model.FollowUser;
import com.rapidapps.instagram.instagram.model.Post;
import com.squareup.picasso.Picasso;

import org.w3c.dom.Text;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link FollowingInstaPost.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link FollowingInstaPost#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FollowingInstaPost extends Fragment {

    private RecyclerView mPosts;
    private DatabaseReference mDatabaseFollowUser;
    private DatabaseReference mDatabaseLike;
    private boolean mFollowProcess = false;

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;

    public FollowingInstaPost() {
        // Required empty public constructor
    }


    public static FollowingInstaPost newInstance() {
        FollowingInstaPost fragment = new FollowingInstaPost();
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater,final ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_following_insta_post, container, false);

        mPosts = (RecyclerView) rootView.findViewById(R.id.followUsersListRecyclerView);
        mPosts.setHasFixedSize(true);
        mPosts.setLayoutManager(new LinearLayoutManager(getContext()));

        mDatabaseFollowUser = FirebaseDatabase.getInstance().getReference().child("Users");
        mDatabaseLike = FirebaseDatabase.getInstance().getReference().child("Likes");
        mDatabaseLike.keepSynced(true);

        mAuth = FirebaseAuth.getInstance();
        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {

                if (firebaseAuth.getCurrentUser() == null) {
                    Intent logIntent = new Intent(getContext(), MainActivity.class);
                    logIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(logIntent);

                }

            }
        };

        FirebaseRecyclerAdapter<FollowUser, FollowingInstaPost.FollowUserViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<FollowUser, FollowingInstaPost.FollowUserViewHolder>(
                FollowUser.class,
                R.layout.follow_user_card,
                FollowingInstaPost.FollowUserViewHolder.class,
                mDatabaseFollowUser

        ) {
            @Override
            protected void populateViewHolder(final FollowingInstaPost.FollowUserViewHolder viewHolder, final FollowUser model, int position) {

                final String userId = getRef(position).getKey();
                Log.d( "userId", userId );


                        String userName = model.getUsername();
                        String userProfilePic = model.getImageurl();

                        viewHolder.setUserName(userName);

                        viewHolder.setProfilePic(container.getContext(), userProfilePic );

                        viewHolder.setFollowSatus( userId );

                        viewHolder.mFollowUserButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {

                                mFollowProcess = true;

                                viewHolder.mDatabaseFollow.addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        if (mFollowProcess == true) {
                                            if (dataSnapshot.hasChild( model.getUserid() )) {

                                                viewHolder.mDatabaseFollow.child( model.getUserid() ).removeValue();
                                                mFollowProcess = false;
                                            } else {

                                                viewHolder.mDatabaseFollow.child( model.getUserid() ).setValue( "Following" );;
                                                mFollowProcess = false;
                                            }

                                        }

                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {

                                    }
                                });


                            }
                        });




            }
        };

        mPosts.setAdapter(firebaseRecyclerAdapter);


        return rootView;
    }

    public static class FollowUserViewHolder extends RecyclerView.ViewHolder {

        View mView;
        CircleImageView profilePicture;
        TextView mFollowUserButton;
        TextView userName;
        DatabaseReference mDatabaseFollow;
        FirebaseAuth mAuth;

        public FollowUserViewHolder(View itemView) {
            super(itemView);

            this.mView = itemView;
            mAuth = FirebaseAuth.getInstance();
            userName = (TextView) mView.findViewById(R.id.followUsername);
            mDatabaseFollow = FirebaseDatabase.getInstance().getReference().child("Users").child(mAuth.getCurrentUser().getUid()).child( "following" );

            mFollowUserButton = (TextView) mView.findViewById(R.id.followStatus);
            profilePicture = (CircleImageView) mView.findViewById( R.id.followUserProfilePicture);

        }


        public void setUserName(String username) {

            userName.setText(username);

        }

        public void FolloUserStatus(String followStatus)
        {
            mFollowUserButton.setText( followStatus );

        }

        public void setProfilePic(android.content.Context ctx, String image) {
            Picasso.with(ctx).load(image).into( profilePicture );
        }


        public void setFollowSatus(final String user_id) {

            mDatabaseFollow.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    if (dataSnapshot.child(user_id).exists() ) {
                        mFollowUserButton.setText( "Following" );
                    }else
                    {
                        mFollowUserButton.setText( "Follow" );
                    }


//                    if (mAuth.getCurrentUser() != null) {
//                        if (dataSnapshot.child(post_id).hasChild(mAuth.getCurrentUser().getUid())) {
//
//                            mLikeButton.setImageResource(R.drawable.ic_favorite_red_24px);
//
//                            long totalNoLikes = dataSnapshot.child(post_id).getChildrenCount();
//                            String like = Long.toString(totalNoLikes);
//                            mTotalLike.setText(like);
//
//                        } else {
//
//                            mLikeButton.setImageResource(R.drawable.ic_favorite_border_black_24px);
//
//                            if (dataSnapshot.child(post_id).getChildrenCount() != 0) {
//                                long totalNoLikes = dataSnapshot.child(post_id).getChildrenCount();
//                                String like = Long.toString(totalNoLikes);
//                                mTotalLike.setText(like);
//                            }
//                        }
//                    }

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

        }


    }


    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
