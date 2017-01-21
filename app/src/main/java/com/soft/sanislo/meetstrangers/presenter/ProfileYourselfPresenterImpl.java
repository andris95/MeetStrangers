package com.soft.sanislo.meetstrangers.presenter;

import com.afollestad.materialdialogs.MaterialDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;
import com.soft.sanislo.meetstrangers.activity.ProfileYourselfActivity;
import com.soft.sanislo.meetstrangers.model.Comment;
import com.soft.sanislo.meetstrangers.model.LocationSnapshot;
import com.soft.sanislo.meetstrangers.model.Post;
import com.soft.sanislo.meetstrangers.model.User;
import com.soft.sanislo.meetstrangers.utilities.Constants;
import com.soft.sanislo.meetstrangers.utilities.Utils;
import com.soft.sanislo.meetstrangers.view.ProfileYourselfView;

import java.util.Date;

/**
 * Created by andras on 19.12.16.
 */

public class ProfileYourselfPresenterImpl implements ProfileYourselfPresenter {
    private static final String TAG = ProfileYourselfPresenterImpl.class.getSimpleName();
    private ProfileYourselfActivity mContext;
    private ProfileYourselfView mView;

    private DatabaseReference mDatabaseRef = Utils.getDatabase().getReference();
    private DatabaseReference mAuthenticatedUserRef;
    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;

    private User mUser;
    private String mAuthenticatedUserUID;

    private MaterialDialog mActionDialog;

    private ValueEventListener mAuthenticatedUserListener = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            mUser = dataSnapshot.getValue(User.class);
            mView.onUserDataChanged(mUser);
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    };

    private ValueEventListener mUserLocationListener = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            LocationSnapshot locationSnapshot = dataSnapshot.getValue(LocationSnapshot.class);
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    };

    public ProfileYourselfPresenterImpl(ProfileYourselfActivity context) {
        mContext = context;
        mView = (ProfileYourselfView) context;

        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();
        mAuthenticatedUserUID = mFirebaseUser.getUid();
        mAuthenticatedUserRef = mDatabaseRef.child(Constants.F_USERS).child(mAuthenticatedUserUID);
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
                        databaseError.toException().printStackTrace();
                    }
                });
    }

    @Override
    public void likePost(String postKey) {
        mDatabaseRef.child(Constants.F_POSTS)
                .child(mAuthenticatedUserUID)
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

            }
        });
    }

    @Override
    public void addComment(Post post, String commentText) {
        DatabaseReference newCommentRef = mDatabaseRef.child(Constants.F_POSTS_COMMENTS)
                .child(post.getAuthorUID())
                .child(post.getKey());
        String newCommentKey = newCommentRef.push().getKey();
        Comment comment = buildNewComment(post, newCommentKey, commentText);
        newCommentRef.child(newCommentKey).setValue(comment);
    }

    private Comment buildNewComment(Post post, String commentKey, String commentText) {
        Comment comment = new Comment(commentKey,
                post.getKey(),
                mAuthenticatedUserUID,
                mUser.getFullName(),
                mUser.getAvatarURL(),
                commentText,
                0,
                null,
                new Date().getTime());
        return comment;
    }

    @Override
    public void onClickFAB() {

    }

    @Override
    public void onResume() {
        mAuthenticatedUserRef.addValueEventListener(mAuthenticatedUserListener);
        mDatabaseRef.child(Constants.F_LOCATIONS).child(mAuthenticatedUserUID)
                .addValueEventListener(mUserLocationListener);
    }

    @Override
    public void onPause() {
        mAuthenticatedUserRef.removeEventListener(mAuthenticatedUserListener);
        mDatabaseRef.child(Constants.F_LOCATIONS)
                .child(mAuthenticatedUserUID)
                .removeEventListener(mUserLocationListener);
    }

    @Override
    public void onBackPressed() {

    }
}
