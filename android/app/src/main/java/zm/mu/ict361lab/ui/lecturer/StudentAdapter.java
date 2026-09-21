package zm.mu.ict361lab.ui.lecturer;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import zm.mu.ict361lab.R;
import zm.mu.ict361lab.data.local.entity.LocalStudentEntity;
import zm.mu.ict361lab.databinding.ItemStudentBinding;
import zm.mu.ict361lab.util.Constants;

public class StudentAdapter extends ListAdapter<LocalStudentEntity, StudentAdapter.Holder> {

    public interface OnClick { void onStudent(LocalStudentEntity student); }

    private final OnClick listener;

    public StudentAdapter(OnClick listener) {
        super(DIFF);
        this.listener = listener;
    }

    private static final DiffUtil.ItemCallback<LocalStudentEntity> DIFF =
            new DiffUtil.ItemCallback<LocalStudentEntity>() {
                @Override
                public boolean areItemsTheSame(@NonNull LocalStudentEntity a, @NonNull LocalStudentEntity b) {
                    return a.studentId.equals(b.studentId);
                }
                @Override
                public boolean areContentsTheSame(@NonNull LocalStudentEntity a, @NonNull LocalStudentEntity b) {
                    return a.version == b.version
                            && equal(a.displayName(), b.displayName())
                            && equal(a.displayGroup(), b.displayGroup())
                            && equal(a.localStatus, b.localStatus);
                }
                private boolean equal(String x, String y) {
                    return x == null ? y == null : x.equals(y);
                }
            };

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new Holder(ItemStudentBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        holder.bind(getItem(position), listener);
    }

    static class Holder extends RecyclerView.ViewHolder {
        private final ItemStudentBinding binding;

        Holder(ItemStudentBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(LocalStudentEntity student, OnClick listener) {
            binding.itemName.setText(student.displayName());

            String group = student.displayGroup();
            String meta = student.studentNumber
                    + " · " + (student.programme == null ? "—" : student.programme)
                    + " · " + (group == null
                        ? itemView.getContext().getString(R.string.label_unassigned) : group);
            binding.itemMeta.setText(meta);

            String status = student.localStatus;
            if (status == null || Constants.LOCAL_SYNCED.equals(status)) {
                binding.itemStatus.setVisibility(View.GONE);
            } else {
                binding.itemStatus.setVisibility(View.VISIBLE);
                binding.itemStatus.setText(labelFor(status));
            }

            // The whole row is the touch target, so it clears 48dp comfortably.
            itemView.setOnClickListener(v -> listener.onStudent(student));
            itemView.setContentDescription(student.displayName() + ", " + meta);
        }

        private int labelFor(String status) {
            switch (status) {
                case Constants.LOCAL_SAVED_LOCAL:     return R.string.status_saved_local;
                case Constants.LOCAL_PENDING:         return R.string.status_pending;
                case Constants.LOCAL_SYNCING:         return R.string.status_syncing;
                case Constants.LOCAL_ACTION_REQUIRED: return R.string.status_action_required;
                default:                              return R.string.status_synced;
            }
        }
    }
}
