package com.soft.sanislo.meetstrangers.presenter;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.soft.sanislo.meetstrangers.model.User;
import com.soft.sanislo.meetstrangers.utilities.Constants;
import com.soft.sanislo.meetstrangers.utilities.Utils;

/**
 * Created by root on 01.11.16.
 */

public class NewPostPresenterImpl implements NewPostPresenter {
    private DatabaseReference mDatabaseReference = Utils.getDatabase().getReference();
    private FirebaseStorage mStorage = FirebaseStorage.getInstance();
    private StorageReference storageRef = mStorage.getReferenceFromUrl(Constants.STORAGE_BUCKET);
    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;
    private User user;
    private String uid;

    private ValueEventListener userValueEventLisener = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            user = dataSnapshot.getValue(User.class);
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {
            databaseError.toException().printStackTrace();
        }
    };

    @Override
    public void onResume() {

    }

    @Override
    public void onPause() {

    }
}
