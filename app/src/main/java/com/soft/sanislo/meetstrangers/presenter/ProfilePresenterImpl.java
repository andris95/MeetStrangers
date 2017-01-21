package com.soft.sanislo.meetstrangers.presenter;

import android.content.Intent;
import android.os.ResultReceiver;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;

import com.afollestad.materialdialogs.MaterialDialog;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;
import com.soft.sanislo.meetstrangers.R;
import com.soft.sanislo.meetstrangers.activity.ChatActivity;
import com.soft.sanislo.meetstrangers.activity.ProfileActivity;
import com.soft.sanislo.meetstrangers.model.Comment;
import com.soft.sanislo.meetstrangers.model.LocationSnapshot;
import com.soft.sanislo.meetstrangers.model.Post;
import com.soft.sanislo.meetstrangers.model.Relationship;
import com.soft.sanislo.meetstrangers.model.User;
import com.soft.sanislo.meetstrangers.utilities.Constants;
import com.soft.sanislo.meetstrangers.utilities.Utils;
import com.soft.sanislo.meetstrangers.view.ProfileView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

/**
 * Created by root on 17.10.16.
 */

public class ProfilePresenterImpl implements ProfilePresenter {
    private static final String TAG = ProfilePresenterImpl.class.getSimpleName();
    private ProfileActivity mContext;
    private ProfileView mProfileView;

    private DatabaseReference mDatabaseRef = Utils.getDatabase().getReference();
    private DatabaseReference mRelationshipRef;
    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;

    private User mAuthenticatedUser;
    private String mAuthenticatedUserUID;
    private User mDisplayedUser;
    private String mDisplayedUserUID;

    /** relationship between the authenticated and displayed users */
    private Relationship mRelationship;
    private String mLastActionUID;

    private ResultReceiver mResultReceiver;
    private MaterialDialog mActionDialog;

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
            mRelationship = dataSnapshot.getValue(Relationship.class);
            if (mRelationship != null) {
                mLastActionUID = mRelationship.getLastActionUserUID();
            }
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
    private DatabaseReference mAuthenticatedUserRef;
    private DatabaseReference mDisplayedUserRef;

    private OnFailureListener mOnFailureListener = new OnFailureListener() {
        @Override
        public void onFailure(@NonNull Exception e) {
            mProfileView.onError(e);
        }
    };

    public ProfilePresenterImpl(ProfileActivity context, ProfileView profileView, String displayedUserUID) {
        mContext = context;
        mProfileView = profileView;
        mDisplayedUserUID = displayedUserUID;
        mDisplayedUserRef = mDatabaseRef.child(Constants.F_USERS).child(mDisplayedUserUID);

        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();
        mAuthenticatedUserUID = mFirebaseUser.getUid();
        mAuthenticatedUserRef = mDatabaseRef.child(Constants.F_USERS).child(mAuthenticatedUserUID);
        mRelationshipRef = mDatabaseRef.child(Constants.F_RELATIONSHIPS)
                .child(mAuthenticatedUserUID)
                .child(mDisplayedUserUID);
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
        String[] profileOptions = mContext.getResources().getStringArray(R.array.profile_stranger_options);
        ArrayList<String> options = new ArrayList<>(Arrays.asList(profileOptions));
        options.add(getRelationshipStatusString());

        mActionDialog = new MaterialDialog.Builder(mContext)
                .items(options)
                .itemsCallback(new MaterialDialog.ListCallback() {
                    @Override
                    public void onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {
                        Log.d(TAG, "onSelection: which: " + which);
                        switch (which) {
                            case 0:
                                launchChatActivity();
                                break;
                            case 1:
                                onClickRelationshipAction();
                                break;
                            default:
                                break;
                        }
                    }
                }).show();
    }

    private void launchChatActivity() {
        Intent intent = new Intent(mContext, ChatActivity.class);
        intent.putExtra(ChatActivity.KEY_CHAT_PARTNER_UID, mDisplayedUserUID);
        mContext.startActivity(intent);
    }

    private String getRelationshipStatusString() {
        if (mRelationship == null) {
            return "Follow";
        } else {
            return mRelationship.getRelationshipStatusString(mAuthenticatedUserUID);
        }
    }

    private void onClickRelationshipAction() {
        if (mRelationship == null) {
            startFollowingDisplayedUser();
        } else {
            if (mRelationship.getStatus() == Constants.RS_FRIENDS) {
                deleteFriend();
            } else if (mRelationship.getStatus() == Constants.RS_PENDING) {
                if (mLastActionUID.equals(mAuthenticatedUserUID)) {
                    stopFollowingDisplayedUser();
                } else {
                    addFriendDisplayedUser();
                }
            } else if (mRelationship.getStatus() == Constants.RS_DELETED) {
                if (mLastActionUID.equals(mAuthenticatedUserUID)) {
                    addFriendDisplayedUser();
                } else {
                    stopFollowingDisplayedUser();
                }
            }
        }
        updateRelationship();
    }

    private void setFriends(String firstUserUID, String secondUserUID, boolean areFriends) {
        mDatabaseRef.child(Constants.F_USERS_FRIENDS)
                .child(firstUserUID)
                .child(secondUserUID)
                .setValue((areFriends) ? true : null);
        mDatabaseRef.child(Constants.F_USERS_FRIENDS)
                .child(secondUserUID)
                .child(firstUserUID)
                .setValue((areFriends) ? true : null);
    }

    private void setFollowers(String userToFollowUID, String followerUID, boolean areFollowers) {
        mDatabaseRef.child(Constants.F_USERS_FOLLOWERS)
                .child(userToFollowUID)
                .child(followerUID).setValue((areFollowers) ? true : null);
        mDatabaseRef.child(Constants.F_USERS_FOLLOWING)
                .child(followerUID)
                .child(userToFollowUID).setValue((areFollowers) ? true : null);
    }

    private void deleteFriend() {
        mRelationship = new Relationship(Constants.RS_DELETED,
                new Date().getTime(),
                mAuthenticatedUserUID,
                mRelationship.getFirstActionUserUID());
        setFriends(mAuthenticatedUserUID, mDisplayedUserUID, false);
        setFollowers(mAuthenticatedUserUID, mDisplayedUserUID, true);
    }

    private void addFriendDisplayedUser() {
        mRelationship = new Relationship(Constants.RS_FRIENDS,
                new Date().getTime(),
                mAuthenticatedUserUID,
                mRelationship.getFirstActionUserUID());
        setFriends(mAuthenticatedUserUID, mDisplayedUserUID, true);
        setFollowers(mAuthenticatedUserUID, mDisplayedUserUID, false);
    }

    private void stopFollowingDisplayedUser() {
        mRelationship = null;
        setFollowers(mDisplayedUserUID, mAuthenticatedUserUID, false);
    }

    private void startFollowingDisplayedUser() {
        mRelationship = new Relationship(Constants.RS_PENDING,
                new Date().getTime(),
                mAuthenticatedUserUID,
                mAuthenticatedUserUID);
        setFollowers(mDisplayedUserUID, mAuthenticatedUserUID, true);
    }

    private void updateRelationship() {
        mDatabaseRef.child(Constants.F_RELATIONSHIPS)
                .child(mAuthenticatedUserUID)
                .child(mDisplayedUserUID)
                .setValue(mRelationship);
        mDatabaseRef.child(Constants.F_RELATIONSHIPS)
                .child(mDisplayedUserUID)
                .child(mAuthenticatedUserUID)
                .setValue(mRelationship);
    }

    @Override
    public void likeComment(Comment comment) {
        mDatabaseRef.child(Constants.F_POSTS_COMMENTS).child(comment.getAuthorUID())
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
        mDatabaseRef.child(Constants.F_LOCATIONS).child(mDisplayedUserUID)
                .addValueEventListener(mUserLocationListener);
        mRelationshipRef.removeEventListener(mRelationshipListener);
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
}
