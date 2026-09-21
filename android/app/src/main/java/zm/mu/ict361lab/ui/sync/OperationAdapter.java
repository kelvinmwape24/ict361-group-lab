package zm.mu.ict361lab.ui.sync;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;

import zm.mu.ict361lab.R;
import zm.mu.ict361lab.data.local.entity.PendingOperationEntity;
import zm.mu.ict361lab.data.remote.Dtos;
import zm.mu.ict361lab.databinding.ItemOperationBinding;
import zm.mu.ict361lab.util.ApiErrors;
import zm.mu.ict361lab.util.Constants;

public class OperationAdapter
        extends ListAdapter<PendingOperationEntity, OperationAdapter.Holder> {

    public interface Actions {
        void keepMine(String operationId);
        void discard(String operationId);
    }

    private static final Gson GSON = new Gson();
    private final Actions actions;

    public OperationAdapter(Actions actions) {
        super(DIFF);
        this.actions = actions;
    }

    private static final DiffUtil.ItemCallback<PendingOperationEntity> DIFF =
            new DiffUtil.ItemCallback<PendingOperationEntity>() {
                @Override public boolean areItemsTheSame(@NonNull PendingOperationEntity a,
                                                         @NonNull PendingOperationEntity b) {
                    return a.operationId.equals(b.operationId);
                }
                @Override public boolean areContentsTheSame(@NonNull PendingOperationEntity a,
                                                            @NonNull PendingOperationEntity b) {
                    return String.valueOf(a.status).equals(String.valueOf(b.status))
                            && String.valueOf(a.lastError).equals(String.valueOf(b.lastError))
                            && String.valueOf(a.serverSnapshot).equals(String.valueOf(b.serverSnapshot));
                }
            };

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new Holder(ItemOperationBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        holder.bind(getItem(position), actions);
    }

    static class Holder extends RecyclerView.ViewHolder {
        private final ItemOperationBinding binding;

        Holder(ItemOperationBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(PendingOperationEntity op, Actions actions) {
            binding.opTitle.setText(titleFor(op.operationType));
            binding.opStatus.setText(statusLabel(op.status));

            boolean needsUser = Constants.LOCAL_ACTION_REQUIRED.equals(op.status);
            binding.opActions.setVisibility(needsUser ? View.VISIBLE : View.GONE);

            if (op.lastError != null) {
                binding.opDetail.setVisibility(View.VISIBLE);
                binding.opDetail.setText(itemView.getContext()
                        .getString(ApiErrors.messageFor(op.lastError)));
            } else {
                binding.opDetail.setVisibility(View.GONE);
            }

            showComparison(op);

            // "Keep my version" only makes sense for a version conflict. For a
            // full group, re-sending the same request would just fail again.
            binding.keepButton.setVisibility(op.serverVersion != null ? View.VISIBLE : View.GONE);
            binding.keepButton.setOnClickListener(v -> actions.keepMine(op.operationId));
            binding.discardButton.setOnClickListener(v -> actions.discard(op.operationId));
        }

        /**
         * The user's unsent change beside the record as the server holds it.
         * Without both sides, "action required" asks someone to choose blind.
         */
        private void showComparison(PendingOperationEntity op) {
            if (op.serverSnapshot == null || op.payload == null) {
                binding.opCompare.setVisibility(View.GONE);
                return;
            }
            try {
                Dtos.SyncPayload mine = GSON.fromJson(op.payload, Dtos.SyncPayload.class);
                Dtos.StudentDto theirs = GSON.fromJson(op.serverSnapshot, Dtos.StudentDto.class);
                if (mine == null || theirs == null) {
                    binding.opCompare.setVisibility(View.GONE);
                    return;
                }
                binding.opMine.setText(describe(mine.name, op.baseVersion));
                binding.opTheirs.setText(describe(theirs.name, theirs.version));
                binding.opCompare.setVisibility(View.VISIBLE);
            } catch (Exception e) {
                // A snapshot we cannot read is not worth showing half of.
                binding.opCompare.setVisibility(View.GONE);
            }
        }

        private String describe(String name, int version) {
            String shown = name == null ? "—" : name;
            return shown + "  ("
                    + itemView.getContext().getString(R.string.fmt_version, version) + ")";
        }

        private int titleFor(String type) {
            if (type == null) return R.string.op_update;
            switch (type) {
                case Constants.OP_CREATE: return R.string.op_create;
                case Constants.OP_ASSIGN: return R.string.op_assign;
                case Constants.OP_DELETE: return R.string.op_delete;
                default:                  return R.string.op_update;
            }
        }

        private int statusLabel(String status) {
            if (status == null) return R.string.status_pending;
            switch (status) {
                case Constants.LOCAL_SYNCING:         return R.string.status_syncing;
                case Constants.LOCAL_SYNCED:          return R.string.status_synced;
                case Constants.LOCAL_ACTION_REQUIRED: return R.string.status_action_required;
                case Constants.LOCAL_SAVED_LOCAL:     return R.string.status_saved_local;
                default:                              return R.string.status_pending;
            }
        }
    }
}
