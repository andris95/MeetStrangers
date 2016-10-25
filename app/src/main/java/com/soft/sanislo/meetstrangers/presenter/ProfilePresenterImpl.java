package com.soft.sanislo.meetstrangers.presenter;

import android.content.Intent;
import android.location.Address;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;

import com.afollestad.materialdialogs.MaterialDialog;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.PlaceLikelihood;
import com.google.android.gms.location.places.PlaceLikelihoodBuffer;
import com.google.android.gms.location.places.Places;
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
import com.soft.sanislo.meetstrangers.model.CommentModel;
import com.soft.sanislo.meetstrangers.model.LocationSnapshot;
import com.soft.sanislo.meetstrangers.model.Post;
import com.soft.sanislo.meetstrangers.model.Relationship;
import com.soft.sanislo.meetstrangers.model.User;
import com.soft.sanislo.meetstrangers.service.FetchAddressIntentService;
import com.soft.sanislo.meetstrangers.utilities.Constants;
import com.soft.sanislo.meetstrangers.utilities.DateUtils;
import com.soft.sanislo.meetstrangers.utilities.LocationUtils;
import com.soft.sanislo.meetstrangers.utilities.Utils;
import com.soft.sanislo.meetstrangers.view.ProfileView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;

/**
 * Created by root on 17.10.16.
 */

public class ProfilePresenterImpl implements ProfilePresenter {
    private static final String TAG = ProfilePresenterImpl.class.getSimpleName();
    private ProfileActivity mContext;
    private ProfileView mProfileView;

    private GoogleApiClient mGoogleApiClient;
    private DatabaseReference mDatabaseRef = Utils.getDatabase().getReference();
    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;

    private User mAuthenticatedUser;
    private String mAuthenticatedUserUID;
    private User mDisplayedUser;
    private String mDisplayedUserUID;

    /** relationship between the authenticated and displayed users */
    private Relationship mRelationship;
    private String mLastActionUID;
    private String mFirstActionUID;

    private ResultReceiver mResultReceiver;
    private MaterialDialog mActionDialog;

    /** ValueEventListener for current logged in user */
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

    /** ValueEventListener for displayed mDisplayedUser*/
    private ValueEventListener mDisplayedUserListener = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            mDisplayedUser = dataSnapshot.getValue(User.class);
            onDisplayedUserChanged(mDisplayedUser);
            mGoogleApiClient.connect();
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
                Log.d(TAG, "onDataChange: " + mRelationship);
                mFirstActionUID = mRelationship.getFirstActionUserUID();
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
            if (locationSnapshot != null) {
                Intent intent = new Intent(mContext, FetchAddressIntentService.class);
                intent.putExtra(FetchAddressIntentService.RECEIVER, mResultReceiver);
                intent.putExtra(FetchAddressIntentService.LOCATION_DATA_EXTRA,
                        LocationUtils.getLocation(locationSnapshot));
                mContext.startService(intent);

                String lastActive = DateUtils.getDateDisplay(locationSnapshot.getTimestamp());
                mProfileView.onLastActiveChanged(lastActive);
            }
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {
            mProfileView.onDatabaseError(databaseError);
        }
    };

    private GoogleApiClient.ConnectionCallbacks mConnectionCallback = new GoogleApiClient.ConnectionCallbacks() {
        @Override
        public void onConnected(@Nullable Bundle bundle) {
            Log.d(TAG, "onConnected: ");
            PendingResult<PlaceLikelihoodBuffer> result = Places.PlaceDetectionApi
                    .getCurrentPlace(mGoogleApiClient, null);
            result.setResultCallback(new ResultCallback<PlaceLikelihoodBuffer>() {
                @Override
                public void onResult(PlaceLikelihoodBuffer likelyPlaces) {
                    Log.d(TAG, "onResult: likelyPlace size: " + likelyPlaces.toString());
                    Status status = likelyPlaces.getStatus();
                    int statusCode = status.getStatusCode();
                    Log.d(TAG, "onResult: statusCOde " + statusCode);
                    for (PlaceLikelihood placeLikelihood : likelyPlaces) {
                        Log.d(TAG, "onResult: place: " + placeLikelihood.getPlace().getName());
                    }
                    likelyPlaces.release();
                }
            });
        }

        @Override
        public void onConnectionSuspended(int i) {

        }
    };

    private GoogleApiClient.OnConnectionFailedListener mConnectionFailedListener = new GoogleApiClient.OnConnectionFailedListener() {
        @Override
        public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
            Log.d(TAG, "onConnectionFailed: " + connectionResult.getErrorMessage());
        }
    };

    public ProfilePresenterImpl(ProfileActivity context, ProfileView profileView, String displayedUserUID) {
        mContext = context;
        mProfileView = profileView;
        mDisplayedUserUID = displayedUserUID;

        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();
        mAuthenticatedUserUID = mFirebaseUser.getUid();

        initAddressFetcher();
    }

    private void initAddressFetcher() {
        mGoogleApiClient = new GoogleApiClient
                .Builder(mContext)
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .addConnectionCallbacks(mConnectionCallback)
                .addOnConnectionFailedListener(mConnectionFailedListener)
                .build();
        mResultReceiver = new AddressResultReceiver(new Handler());
    }

    @Override
    public void addComment(Post post, String commentText) {
        DatabaseReference newCommentRef = mDatabaseRef.child(Constants.F_POSTS_COMMENTS)
                .child(post.getAuthorUID())
                .child(post.getKey());
        String newCommentKey = newCommentRef.push().getKey();
        CommentModel commentModel = new CommentModel(newCommentKey,
                post.getKey(),
                mAuthenticatedUserUID,
                mAuthenticatedUser.getFullName(),
                mAuthenticatedUser.getAvatarURL(),
                commentText,
                new Date().getTime());
        newCommentRef.child(newCommentKey).setValue(commentModel, 0 - commentModel.getTimestamp())
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        e.printStackTrace();
                        mProfileView.onError(e.getMessage());
                    }
                });
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
            switch (mRelationship.getStatus()) {
                case Constants.RS_FRIENDS:
                    return "Delete from friends";
                case Constants.RS_PENDING:
                    if (mLastActionUID.equals(mAuthenticatedUserUID)) {
                        return "Cancel follow request";
                    } else {
                        return "Accept follow request";
                    }
                case Constants.RS_DELETED:
                    if (mLastActionUID.equals(mAuthenticatedUserUID)) {
                        return "Accept follow request";
                    } else {
                        return "Cancel follow request";
                    }
                default:
                    return "UNKNOWN";
            }
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
    public void likeComment(CommentModel commentModel) {
        if (commentModel.getLikedUsersUIDs() == null) commentModel.setLikedUsersUIDs(new HashMap<String, Boolean>());
        commentModel.getLikedUsersUIDs().put(mAuthenticatedUserUID, isCommentLikedByUser(commentModel) ? null
            : true);

        HashMap<String, Object> childUpdates = new HashMap<>();
        childUpdates.put("likedUsersUIDs", commentModel.getLikedUsersUIDs());
        Utils.getDatabase().getReference()
                .child(Constants.F_POSTS_COMMENTS)
                .child(commentModel.getAuthorUID())
                .child(commentModel.getPostKey())
                .child(commentModel.getCommentKey())
                .updateChildren(childUpdates);
    }

    private boolean isCommentLikedByUser(CommentModel commentModel) {
        HashMap<String, Boolean> likers = commentModel.getLikedUsersUIDs();
        if (likers == null) return false;
        return likers.containsKey(mAuthenticatedUserUID);
    }

    @Override
    public void likePost(String postKey) {
        mDatabaseRef.child(Constants.F_POSTS)
                .child(mDisplayedUserUID)
                .child(postKey).runTransaction(new Transaction.Handler() {
            @Override
            public Transaction.Result doTransaction(MutableData mutableData) {
                Post post = mutableData.getValue(Post.class);
                if (post == null) {
                    return Transaction.success(mutableData);
                } else {
                    post = likePost(post);
                    mutableData.setValue(post);
                    mutableData.setPriority(0 - post.getTimestamp());
                    return Transaction.success(mutableData);
                }
            }

            @Override
            public void onComplete(DatabaseError databaseError, boolean b, DataSnapshot dataSnapshot) {
                if (databaseError != null) {
                    databaseError.toException().printStackTrace();
                    mProfileView.onDatabaseError(databaseError);
                }
            }
        });
    }

    private Post likePost(Post post) {
        long postLikesCount = post.getLikesCount();
        HashMap<String, Boolean> likedUsersUIDs = post.getLikedUsersUIDs();
        if (likedUsersUIDs != null &&
                likedUsersUIDs.containsKey(mAuthenticatedUserUID)) {
            post.setLikesCount(postLikesCount - 1);
            likedUsersUIDs.remove(mAuthenticatedUserUID);
        } else {
            post.setLikesCount(postLikesCount + 1);
            if (likedUsersUIDs == null) likedUsersUIDs = new HashMap<>();
            likedUsersUIDs.put(mAuthenticatedUserUID, true);
        }
        post.setLikedUsersUIDs(likedUsersUIDs);
        return post;
    }

    @Override
    public void onDisplayedUserChanged(User user) {
        mProfileView.onDisplayedUserChanged(user);
    }

    @Override
    public void onResume() {
        mDatabaseRef.child(Constants.F_USERS).child(mAuthenticatedUserUID)
                .addValueEventListener(mAuthenticatedUserListener);
        mDatabaseRef.child(Constants.F_USERS).child(mDisplayedUserUID)
                .addValueEventListener(mDisplayedUserListener);
        mDatabaseRef.child(Constants.F_LOCATIONS).child(mDisplayedUserUID)
                .addValueEventListener(mUserLocationListener);
        mDatabaseRef.child(Constants.F_RELATIONSHIPS)
                .child(mAuthenticatedUserUID)
                .child(mDisplayedUserUID)
                .addValueEventListener(mRelationshipListener);
    }

    @Override
    public void onPause() {
        mGoogleApiClient.disconnect();
        mDatabaseRef.child(Constants.F_USERS).child(mAuthenticatedUserUID)
                .removeEventListener(mAuthenticatedUserListener);
        mDatabaseRef.child(Constants.F_USERS).child(mDisplayedUserUID)
                .removeEventListener(mDisplayedUserListener);
        mDatabaseRef.child(Constants.F_LOCATIONS).child(mDisplayedUserUID)
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

    private class AddressResultReceiver extends ResultReceiver {

        AddressResultReceiver(Handler handler) {
            super(handler);
        }

        @Override
        protected void onReceiveResult(final int resultCode, final Bundle resultData) {
            if (resultCode == FetchAddressIntentService.SUCCESS_RESULT) {
                final Address address = resultData.getParcelable(FetchAddressIntentService.RESULT_ADDRESS);
                if (address != null) {
                    String featureName = address.getFeatureName();
                    mProfileView.onAddressFetchSuccess(featureName);
                }
            } else if (resultCode == FetchAddressIntentService.FAILURE_RESULT) {
                String errorMessage = resultData.getString(FetchAddressIntentService.RESULT_DATA_KEY);
                mProfileView.onAddressFetchFailure(errorMessage);
            }
        }
    }
}
