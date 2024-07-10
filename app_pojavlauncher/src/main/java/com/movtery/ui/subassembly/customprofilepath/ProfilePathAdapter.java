package com.movtery.ui.subassembly.customprofilepath;

import static net.kdt.pojavlaunch.prefs.LauncherPreferences.DEFAULT_PREF;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import net.kdt.pojavlaunch.R;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

public class ProfilePathAdapter extends RecyclerView.Adapter<ProfilePathAdapter.ViewHolder> {
    private final Map<String, RadioButton> radioButtonMap = new TreeMap<>();
    private final RecyclerView view;
    private List<ProfileItem> mData;
    private String currentId;

    public ProfilePathAdapter(RecyclerView view, List<ProfileItem> mData) {
        this.mData = mData;
        this.view = view;
        this.currentId = DEFAULT_PREF.getString("launcherProfile", "default");
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_profile_path, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.setView(mData.get(position), position);
    }

    @Override
    public int getItemCount() {
        if (mData != null) {
            return mData.size();
        }
        return 0;
    }

    @SuppressLint("NotifyDataSetChanged")
    public void updateData(List<ProfileItem> mData) {
        this.mData = mData;

        ProfilePathManager.save(this.mData);

        notifyDataSetChanged();
        this.view.scheduleLayoutAnimation();
    }

    private void setPathId(String id) {
        currentId = id;
        ProfilePathManager.setCurrentPathId(id);
        updateRadioButtonState(id);
    }

    private void updateRadioButtonState(String id) {
        //遍历全部RadioButton，取消除去此id的全部RadioButton
        for (Map.Entry<String, RadioButton> entry : radioButtonMap.entrySet()) {
            entry.getValue().setChecked(Objects.equals(id, entry.getKey()));
        }
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private final RadioButton mRadioButton;
        private final TextView mTitle, mPath;
        private final ImageButton mRenameButton, mDeleteButton;
        private final View itemView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            this.itemView = itemView;

            mRadioButton = itemView.findViewById(R.id.profile_path_radio_button);
            mTitle = itemView.findViewById(R.id.profile_path_title);
            mPath = itemView.findViewById(R.id.profile_path_path);
            mRenameButton = itemView.findViewById(R.id.profile_path_rename);
            mDeleteButton = itemView.findViewById(R.id.profile_path_delete);
        }

        public void setView(ProfileItem profileItem, int position) {
            radioButtonMap.put(profileItem.id, mRadioButton);
            mTitle.setText(profileItem.title);
            mPath.setText(profileItem.path);

            View.OnClickListener onClickListener = v -> setPathId(profileItem.id);
            itemView.setOnClickListener(onClickListener);
            mRadioButton.setOnClickListener(onClickListener);

            mRenameButton.setOnClickListener(v -> {
                if (!profileItem.id.equals("default")) {
                    Context context = mRenameButton.getContext();

                    final EditText edit = new EditText(context);
                    edit.setSingleLine();
                    edit.setText(profileItem.title);

                    androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(context);
                    builder.setView(edit).setTitle(R.string.global_rename);
                    builder.setPositiveButton(android.R.string.ok, (dialog, which) -> {
                        String string = edit.getText().toString();
                        if (string.isEmpty()) {
                            edit.setError(context.getString(R.string.global_error_field_empty));
                            return;
                        }

                        mData.get(position).title = string;
                        updateData(mData);
                    });
                    builder.setNegativeButton(android.R.string.cancel, null);

                    builder.show();
                }
            });

            mDeleteButton.setOnClickListener(v -> {
                if (!profileItem.id.equals("default")) {
                    Context context = mDeleteButton.getContext();
                    new AlertDialog.Builder(context)
                            .setTitle(R.string.profiles_path_delete_title)
                            .setMessage(R.string.profiles_path_delete_message)
                            .setPositiveButton(R.string.global_delete, (dialog, which) -> {
                                if (Objects.equals(currentId, profileItem.id)) {
                                    //If the currently selected path is deleted, it is automatically selected as the default path
                                    setPathId("default");
                                }
                                mData.remove(position);
                                updateData(mData);
                            })
                            .setNegativeButton(android.R.string.cancel, null)
                            .show();
                }
            });

            if (profileItem.id.equals("default")) {
                mRenameButton.setVisibility(View.GONE);
                mDeleteButton.setVisibility(View.GONE);
            }

            if (Objects.equals(currentId, profileItem.id)) {
                updateRadioButtonState(profileItem.id);
            }
        }
    }
}
