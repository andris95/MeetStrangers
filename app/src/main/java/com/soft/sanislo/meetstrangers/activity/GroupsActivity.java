package com.soft.sanislo.meetstrangers.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.BoolRes;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.soft.sanislo.meetstrangers.R;
import com.soft.sanislo.meetstrangers.adapter.ChatMessageAdapter;
import com.soft.sanislo.meetstrangers.adapter.GroupsAdapter;
import com.soft.sanislo.meetstrangers.model.ChatMessage;
import com.soft.sanislo.meetstrangers.model.Group;
import com.soft.sanislo.meetstrangers.utilities.Constants;
import com.soft.sanislo.meetstrangers.utilities.Utils;
import com.soft.sanislo.meetstrangers.view.ChatMessageViewHolder;
import com.soft.sanislo.meetstrangers.view.GroupViewHolder;

import java.util.Date;
import java.util.HashMap;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by root on 04.12.16.
 */

public class GroupsActivity extends BaseActivity {
    public static final String TAG = GroupsActivity.class.getSimpleName();

    @BindView(R.id.rv_groups)
    RecyclerView rvGroups;

    @BindView(R.id.toolbar)
    Toolbar mToolbar;

    private GroupsAdapter mGroupsAdapter;
    private DatabaseReference mDatabaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_groups);
        ButterKnife.bind(this);
        setSupportActionBar(mToolbar);
        mDatabaseReference = Utils.getDatabase().getReference();
        initGroups();
    }

    private void initGroups() {
        mGroupsAdapter = new GroupsAdapter(Group.class,
                R.layout.item_group,
                GroupViewHolder.class,
                mDatabaseReference.child(Constants.F_GROUPS));
        mGroupsAdapter.setOnClickListener(new GroupsAdapter.OnClickListener() {
            @Override
            public void onClick(View view, Group group, int position) {
                Intent intent = new Intent(GroupsActivity.this, GroupProfileActivity.class);
                startActivity(intent);
            }
        });
        rvGroups.setLayoutManager(new LinearLayoutManager(this));
        rvGroups.setAdapter(mGroupsAdapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_groups, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_new_group:
                onClickNewGroup();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void onClickNewGroup() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_new_group, null);
        final EditText edtGroupName = (EditText) dialogView.findViewById(R.id.edt_group_name);

        new MaterialDialog.Builder(this)
                .title("New group")
                .customView(dialogView, false)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        String groupName = edtGroupName.getText().toString();
                        Log.d(TAG, "onClick: groupName: " + groupName);
                        createNewGroup(groupName);
                    }
                })
                .positiveText("ok")
                .negativeText("cancel")
                .show();
    }

    private void createNewGroup(String name) {
        Group group = new Group();
        String groupKey = mDatabaseReference.child(Constants.F_GROUPS).push().getKey();
        group.setGroupID(groupKey);
        group.setOwnerUID(getAuthenticatedUserUID());
        group.setCreatedAt(new Date().getTime());
        group.setName(name);
        HashMap<String, Boolean> members = new HashMap<>();
        members.put(getAuthenticatedUserUID(), true);
        group.setMembers(members);
        mDatabaseReference.child(Constants.F_GROUPS)
                .child(groupKey)
                .setValue(group);
        mDatabaseReference.child(Constants.F_USERS_GROUPS)
                .child(getAuthenticatedUserUID())
                .child(groupKey)
                .setValue(true);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mGroupsAdapter.cleanup();
    }
}
