package com.rapidapps.instagram.instagram.instagramposthome;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.alexzh.circleimageview.CircleImageView;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class InstagramPost extends Fragment {

    private RecyclerView mPosts;
    private DatabaseReference mDatabase;
    private DatabaseReference mTemporaryPostDatabase;
    private DatabaseReference mDatabaseUsers;
    private DatabaseReference mDatabaseLike;
    private DatabaseReference mDatabaseFollow;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;
    private boolean mlikeProcess = false;
    FirebaseRecyclerAdapter<Post, PostViewHolder> firebaseRecyclerAdapter;
    final List<Integer> removedPostIndex = new ArrayList<Integer>();
    String tempPostDatabaseKey;

    public InstagramPost() {
        // Required empty public constructor
    }

    // TODO: Rename and change types and number of parameters
    public static InstagramPost newInstance() {

        InstagramPost fragment = new InstagramPost();
        return fragment;

    }


    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_instagram_post, container, false);

        mPosts = (RecyclerView) rootView.findViewById(R.id.userFeedPostsRecyclerView);
        mPosts.setHasFixedSize(true);
        mPosts.setLayoutManager(new LinearLayoutManager(getContext()));

        mDatabase = FirebaseDatabase.getInstance().getReference().child("Post");
        mDatabase.keepSynced(true);

        mDatabaseUsers = FirebaseDatabase.getInstance().getReference().child("Users");
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

        tempPostDatabaseKey = "Post" + mAuth.getCurrentUser().getUid();
        mTemporaryPostDatabase = FirebaseDatabase.getInstance().getReference().child( tempPostDatabaseKey);

        //GET THE LIST OF USERS CURRENTLY FOLLOWED BY LOGGEDIN USER
        mDatabaseFollow = FirebaseDatabase.getInstance().getReference().child("Users").child(mAuth.getCurrentUser().getUid()).child( "following" );

        final List<String> followingUsers = new ArrayList<String>();

//        mDatabaseFollow.addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(DataSnapshot dataSnapshot) {
//
//                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
//                    String user = snapshot.getValue(String.class);
//                    Log.d( "Following User" , snapshot.getKey());
//                    followingUsers.add( snapshot.getKey() );
//                }
//
//            }
//
//            @Override
//            public void onCancelled(DatabaseError databaseError) {
//
//            }
//        });

        mDatabaseFollow.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                    Log.d( "harharhar" , dataSnapshot.getKey());
                    followingUsers.add( dataSnapshot.getKey().toString() );

                mTemporaryPostDatabase.removeValue();

                //FILTER THE POST ACCORDING TO THE USERS FOLLOWED BY CURRENT USER
                mDatabase.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {

                            Post post = snapshot.getValue(Post.class);

                            if (followingUsers.contains(post.getUserid())) {
                                Log.d("Tester", snapshot.getKey());

                                DatabaseReference mTemp = mTemporaryPostDatabase.child(snapshot.getKey());
                                mTemp.child("caption_text").setValue(post.getCaption_text());
                                mTemp.child("image").setValue(post.getImage());
                                mTemp.child("userid").setValue(post.getUserid());
                                mTemp.child("username").setValue(post.getUsername());

                            }
                        }

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
               String user = dataSnapshot.getValue(String.class);
                    Log.d( "harharhar" , dataSnapshot.getKey());
                    followingUsers.remove( dataSnapshot.getKey().toString() );
                    mTemporaryPostDatabase.removeValue();

                //FILTER THE POST ACCORDING TO THE USERS FOLLOWED BY CURRENT USER
                mDatabase.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {

                            Post post = snapshot.getValue(Post.class);

                            if (followingUsers.contains(post.getUserid())) {
                                Log.d("Tester", snapshot.getKey());

                                DatabaseReference mTemp = mTemporaryPostDatabase.child(snapshot.getKey());
                                mTemp.child("caption_text").setValue(post.getCaption_text());
                                mTemp.child("image").setValue(post.getImage());
                                mTemp.child("userid").setValue(post.getUserid());
                                mTemp.child("username").setValue(post.getUsername());
                            }
                        }

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


        //FILTER THE POST ACCORDING TO THE USERS FOLLOWED BY CURRENT USER
        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {

                    Post post = snapshot.getValue(Post.class);

                    if (followingUsers.contains(post.getUserid())) {
                        Log.d("Tester", snapshot.getKey());

                        DatabaseReference mTemp = mTemporaryPostDatabase.child(snapshot.getKey());
                        mTemp.child("caption_text").setValue(post.getCaption_text());
                        mTemp.child("image").setValue(post.getImage());
                        mTemp.child("userid").setValue(post.getUserid());
                        mTemp.child("username").setValue(post.getUsername());
                    }
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

       //FILL FIREBASE DATABASE WITH DATASET
       firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Post, PostViewHolder>(
                Post.class,
                R.layout.post_card,
                PostViewHolder.class,
                mDatabase
        ) {

           View view;
           @Override
           public PostViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

               view = LayoutInflater.from(parent.getContext())
                       .inflate(R.layout.post_card, parent, false);

               return new PostViewHolder(view);

           }

           @Override
            protected void populateViewHolder(final PostViewHolder viewHolder, final Post model, final int position) {

                final String post_key_id = getRef(position).getKey();

                DatabaseReference mUserInfo = FirebaseDatabase.getInstance().getReference().child("Users").child(model.getUserid());

                mUserInfo.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {


                        String userd = dataSnapshot.child("userid").getValue(String.class);

                        viewHolder.serUserId( userd );

                        if( followingUsers.contains( userd ) )
                        {
                            Log.d( "UserExistance", "Exsts user" );
                        }else
                        {
                            removedPostIndex.add( position );

                            view.setEnabled( false );
                            view.destroyDrawingCache();
                            viewHolder.itemView.setVisibility( View.INVISIBLE );
                            return;
                        }

                        String userName = dataSnapshot.child("username").getValue(String.class);
                        String userProfilePic = dataSnapshot.child( "imageurl" ).getValue( String.class );

                        viewHolder.setUserName(userName);

                        viewHolder.setProfilePic(container.getContext(), userProfilePic );

                        viewHolder.setImage(container.getContext(), model.getImage());

                        viewHolder.setLike(post_key_id);

                        viewHolder.mLikeButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {

                                mlikeProcess = true;

                                mDatabaseLike.addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        if (mlikeProcess == true) {
                                            if (dataSnapshot.child(post_key_id).hasChild(mAuth.getCurrentUser().getUid())) {

                                                mDatabaseLike.child(post_key_id).child(mAuth.getCurrentUser().getUid()).removeValue();
                                                mlikeProcess = false;
                                            } else {

                                                mDatabaseLike.child(post_key_id).child(mAuth.getCurrentUser().getUid()).setValue("RandomValue");
                                                mlikeProcess = false;
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

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

            }
        };

        mPosts.setAdapter(firebaseRecyclerAdapter);

        return rootView;
    }

    @Override
    public void startActivity(Intent intent) {
        super.startActivity(intent);

        mAuth.addAuthStateListener(mAuthStateListener);

    }

    public static class PostViewHolder extends RecyclerView.ViewHolder {

        View mView;
        CircleImageView profilePicture;
        ImageView mLikeButton;
        TextView mTotalLike;
        DatabaseReference mDatabaseLike;
        FirebaseAuth mAuth;
        String userId;

        public PostViewHolder(View itemView) {
            super(itemView);
            this.mView = itemView;

            mLikeButton = (ImageView) mView.findViewById(R.id.userFredToggleLikeButton);
            mDatabaseLike = FirebaseDatabase.getInstance().getReference().child("Likes");
            mAuth = FirebaseAuth.getInstance();
            mTotalLike = (TextView) mView.findViewById(R.id.userFeedNumberOfLikes);
            profilePicture = (CircleImageView) mView.findViewById( R.id.userFeedProfileImage );

        }


        public void setUserName(String userName) {
            TextView post_user_name = (TextView) mView.findViewById(R.id.userFeedProfileName);
            post_user_name.setText(userName);

        }

        public void serUserId( String userId )
        {
            this.userId = userId;

        }

        public String getUserId()
        {
            return this.userId;
        }

        public void setTotalLikes(String totalNumberOfLikes)
        {
            mTotalLike.setText( totalNumberOfLikes );

        }

        public void setImage(android.content.Context ctx, String image) {
            ImageView imageView = (ImageView) mView.findViewById(R.id.userFeedImageView);
            Picasso.with(ctx).load(image).into(imageView);
        }

        public void setProfilePic(android.content.Context ctx, String image) {
            Picasso.with(ctx).load(image).into( profilePicture );
        }

        public void setUsername(String username) {

            TextView user = (TextView) mView.findViewById(R.id.userFeedProfileName);
            user.setText(username);

        }



        public void setLike(final String post_id) {

            mDatabaseLike.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    if (dataSnapshot.child(post_id).getChildrenCount() != 0) {
                        long totalNoLikes = dataSnapshot.child(post_id).getChildrenCount();
                        String like = Long.toString(totalNoLikes);
                        mTotalLike.setText(like);
                    }else
                    {
                        mTotalLike.setText( "0" );
                    }


                    if (mAuth.getCurrentUser() != null) {
                        if (dataSnapshot.child(post_id).hasChild(mAuth.getCurrentUser().getUid())) {

                            mLikeButton.setImageResource(R.drawable.ic_favorite_red_24px);

                            long totalNoLikes = dataSnapshot.child(post_id).getChildrenCount();
                            String like = Long.toString(totalNoLikes);
                            mTotalLike.setText(like);

                        } else {

                            mLikeButton.setImageResource(R.drawable.ic_favorite_border_black_24px);

                            if (dataSnapshot.child(post_id).getChildrenCount() != 0) {
                                long totalNoLikes = dataSnapshot.child(post_id).getChildrenCount();
                                String like = Long.toString(totalNoLikes);
                                mTotalLike.setText(like);
                            }
                        }
                    }

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

        }


    }

}
