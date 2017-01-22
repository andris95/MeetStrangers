package com.soft.sanislo.meetstrangers.presenter;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;
import com.soft.sanislo.meetstrangers.activity.ChatActivity;
import com.soft.sanislo.meetstrangers.activity.ProfileActivity;
import com.soft.sanislo.meetstrangers.model.Comment;
import com.soft.sanislo.meetstrangers.model.LocationSnapshot;
import com.soft.sanislo.meetstrangers.model.Post;
import com.soft.sanislo.meetstrangers.model.RelationshipV2;
import com.soft.sanislo.meetstrangers.model.User;
import com.soft.sanislo.meetstrangers.utilities.Constants;
import com.soft.sanislo.meetstrangers.utilities.Utils;
import com.soft.sanislo.meetstrangers.view.ProfileView;

import java.util.Date;
import java.util.HashMap;

/**
 * Created by root on 17.10.16.
 */

public class ProfilePresenterImpl implements ProfilePresenter {
    private static final String TAG = ProfilePresenterImpl.class.getSimpleName();
    private ProfileActivity mContext;
    private ProfileView mProfileView;

    private DatabaseReference mDatabaseRef = Utils.getDatabase().getReference();

    private User mAuthenticatedUser;
    private String mAuthenticatedUserUID;
    private User mDisplayedUser;
    private String mDisplayedUserUID;

    private DatabaseReference mAuthenticatedUserRef;
    private DatabaseReference mDisplayedUserRef;

    /** relationship between the authenticated and displayed users */
    private RelationshipV2 mRelationship;

    private ValueEventListener mAuthenticatedUserListener = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            mAuthenticatedUser = dataSnapshot.getValue(User.class);
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {
            mProfileView.onDatabaseError(databaseError);
        }
    };

    private ValueEventListener mDisplayedUserListener = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            mDisplayedUser = dataSnapshot.getValue(User.class);
            mProfileView.onDisplayedUserChanged(mDisplayedUser);
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {
            mProfileView.onDatabaseError(databaseError);
        }
    };

    private ValueEventListener mRelationshipListener = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            mRelationship = dataSnapshot.getValue(RelationshipV2.class);
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {
            mProfileView.onDatabaseError(databaseError);
        }
    };

    private ValueEventListener mUserLocationListener = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            LocationSnapshot locationSnapshot = dataSnapshot.getValue(LocationSnapshot.class);
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {
            mProfileView.onDatabaseError(databaseError);
        }
    };

    private OnFailureListener mOnFailureListener = new OnFailureListener() {
        @Override
        public void onFailure(@NonNull Exception e) {
            mProfileView.onError(e);
        }
    };

    public ProfilePresenterImpl(ProfileActivity context,
                                ProfileView profileView) {
        mContext = context;
        mProfileView = profileView;
    }

    @Override
    public void setAuthenticatedUserUID(String authenticatedUserUID) {
        mAuthenticatedUserUID = authenticatedUserUID;
        mAuthenticatedUserRef = mDatabaseRef.child(Constants.F_USERS).child(mAuthenticatedUserUID);
    }

    @Override
    public void setDisplayedUserUID(String uid) {
        mDisplayedUserUID = uid;
        mDisplayedUserRef = mDatabaseRef.child(Constants.F_USERS).child(mDisplayedUserUID);
    }

    @Override
    public void addComment(Post post, String commentText) {
        DatabaseReference newCommentRef = mDatabaseRef.child(Constants.F_POSTS_COMMENTS)
                .child(post.getAuthorUID())
                .child(post.getKey());
        String newCommentKey = newCommentRef.push().getKey();
        Comment comment = buildNewComment(post, newCommentKey, commentText);
        newCommentRef.child(newCommentKey).setValue(comment)
                .addOnFailureListener(mOnFailureListener);
    }

    private Comment buildNewComment(Post post, String commentKey, String commentText) {
        Comment comment = new Comment(commentKey,
                post.getKey(),
                mAuthenticatedUserUID,
                mAuthenticatedUser.getFullName(),
                mAuthenticatedUser.getAvatarURL(),
                commentText,
                0,
                null,
                new Date().getTime());
        return comment;
    }

    @Override
    public void onClickFAB() {
        /*String[] profileOptions = mContext.getResources().getStringArray(R.array.profile_stranger_options);
        ArrayList<String> options = new ArrayList<>(Arrays.asList(profileOptions));
        options.add(getRelationshipStatusText());*/
        mProfileView.showDialog(getRelationshipStatusText());
    }

    @Override
    public void onDialogItemSelected(int position) {
        //for now, position does not matter
        if (mRelationship == null) {
            Log.d(TAG, "onDialogItemSelected: users are strangers, follow this user");
            followDisplayedUser();
        } else {
            if (mRelationship.areFriends()) {
                Log.d(TAG, "onDialogItemSelected: users are friends, unfriend them");
            } else if (mRelationship.isHeFollowingMe(mAuthenticatedUserUID)) {
                Log.d(TAG, "onDialogItemSelected: displayed user is following authenticated user," +
                        "accept follow request, " +
                        "or decline!");
            } else if (mRelationship.isMeFollowingHim(mAuthenticatedUserUID)) {
                Log.d(TAG, "onDialogItemSelected: authenticated user " +
                        "is following the displayed user, unfollow him");
                unfollowDisplayedUser();
            }
        }
    }

    private void followDisplayedUser() {
        HashMap<String, Object> toUpdate = RelationshipV2.getFollowMap(mDisplayedUserUID, mAuthenticatedUserUID);
        mDatabaseRef.updateChildren(toUpdate).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                Log.d(TAG, "onComplete: ");
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d(TAG, "onFailure: ");
                e.printStackTrace();
            }
        });
    }

    private void unfollowDisplayedUser() {
        HashMap<String, Object> toUpdate = RelationshipV2.getUnfollowMap(mDisplayedUserUID, mAuthenticatedUserUID);
        mDatabaseRef.updateChildren(toUpdate).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                Log.d(TAG, "onComplete: ");
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d(TAG, "onFailure: ");
                e.printStackTrace();
            }
        });
    }

    private String getRelationshipStatusText() {
        if (mRelationship == null) {
            return "You are strangers. Follow this user";
        } else {
            return mRelationship.getRelationshipStatusText(mAuthenticatedUserUID);
        }
    }

    private void launchChatActivity() {
        Intent intent = new Intent(mContext, ChatActivity.class);
        intent.putExtra(ChatActivity.KEY_CHAT_PARTNER_UID, mDisplayedUserUID);
        mContext.startActivity(intent);
    }

    @Override
    public void likeComment(Comment comment) {
        mDatabaseRef.child(Constants.F_POSTS_COMMENTS)
                .child(comment.getAuthorUID())
                .child(comment.getPostKey())
                .child(comment.getCommentKey())
                .runTransaction(new Transaction.Handler() {
                    @Override
                    public Transaction.Result doTransaction(MutableData mutableData) {
                        Comment comment = mutableData.getValue(Comment.class);
                        if (comment == null) return Transaction.success(mutableData);
                        comment.setLikedByUser(mAuthenticatedUserUID);
                        mutableData.setValue(comment);
                        return Transaction.success(mutableData);
                    }

                    @Override
                    public void onComplete(DatabaseError databaseError, boolean b, DataSnapshot dataSnapshot) {
                        if (databaseError != null) {
                            mProfileView.onError(databaseError.getMessage());
                        }
                    }
                });
    }

    @Override
    public void likePost(String postKey) {
        mDatabaseRef.child(Constants.F_POSTS)
                .child(mDisplayedUserUID)
                .child(postKey).runTransaction(new Transaction.Handler() {
            @Override
            public Transaction.Result doTransaction(MutableData mutableData) {
                Post post = mutableData.getValue(Post.class);
                if (post == null) return Transaction.success(mutableData);
                post.setLikedByUser(mAuthenticatedUserUID);
                mutableData.setValue(post);
                mutableData.setPriority(0 - post.getTimestamp());
                return Transaction.success(mutableData);
            }

            @Override
            public void onComplete(DatabaseError databaseError, boolean b, DataSnapshot dataSnapshot) {
                if (databaseError != null) {
                    mProfileView.onDatabaseError(databaseError);
                }
            }
        });
    }

    @Override
    public void onResume() {
        mAuthenticatedUserRef.addValueEventListener(mAuthenticatedUserListener);
        mDisplayedUserRef.addValueEventListener(mDisplayedUserListener);
        mDatabaseRef.child(Constants.F_LOCATIONS)
                .child(mDisplayedUserUID)
                .addValueEventListener(mUserLocationListener);
        mDatabaseRef.child(Constants.F_RELATIONSHIPS)
                .child(mAuthenticatedUserUID)
                .child(mDisplayedUserUID)
                .addValueEventListener(mRelationshipListener);
    }

    @Override
    public void onPause() {
        mAuthenticatedUserRef.removeEventListener(mAuthenticatedUserListener);
        mDisplayedUserRef.removeEventListener(mDisplayedUserListener);
        mDatabaseRef.child(Constants.F_LOCATIONS)
                .child(mDisplayedUserUID)
                .removeEventListener(mUserLocationListener);
        mDatabaseRef.child(Constants.F_RELATIONSHIPS)
                .child(mAuthenticatedUserUID)
                .child(mDisplayedUserUID)
                .removeEventListener(mRelationshipListener);
    }

    @Override
    public void onBackPressed() {
        mContext.supportFinishAfterTransition();
    }

    private void areFriends() {
        mDatabaseRef.child(Constants.F_USERS_FRIENDS)
                .child(mAuthenticatedUserUID)
                .child(mDisplayedUserUID)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        boolean areFriends;
                        Log.d(TAG, "onDataChange: " + (dataSnapshot == null));
                        Log.d(TAG, "onDataChange: " + dataSnapshot.getValue());
                        Log.d(TAG, "onDataChange: " + (dataSnapshot.getValue() == null));
                        if (dataSnapshot.getValue() != null) {
                            Log.d(TAG, "onDataChange: " + dataSnapshot.getValue().toString());
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }
}
