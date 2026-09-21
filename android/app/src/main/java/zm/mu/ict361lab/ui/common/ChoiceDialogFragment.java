package zm.mu.ict361lab.ui.common;

import android.app.Dialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;

import zm.mu.ict361lab.R;

/**
 * Pick one item from a list, and survive a rotation while doing it.
 *
 * The labels are passed in as arguments rather than captured from the calling
 * Activity, so the dialog can rebuild itself after the Activity is recreated.
 */
public class ChoiceDialogFragment extends DialogFragment {

    public static final String RESULT_INDEX = "index";

    private static final String ARG_REQUEST = "request";
    private static final String ARG_TITLE   = "title";
    private static final String ARG_ITEMS   = "items";

    public static void show(@NonNull FragmentManager manager, @NonNull String requestKey,
                            int titleRes, @NonNull String[] items) {
        ChoiceDialogFragment f = new ChoiceDialogFragment();
        Bundle args = new Bundle();
        args.putString(ARG_REQUEST, requestKey);
        args.putInt(ARG_TITLE, titleRes);
        args.putStringArray(ARG_ITEMS, items);
        f.setArguments(args);
        f.show(manager, requestKey);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Bundle args = requireArguments();
        final String requestKey = args.getString(ARG_REQUEST, "choice");
        String[] items = args.getStringArray(ARG_ITEMS);
        if (items == null) items = new String[0];

        return new AlertDialog.Builder(requireContext())
                .setTitle(args.getInt(ARG_TITLE))
                .setItems(items, (d, which) -> {
                    Bundle result = new Bundle();
                    result.putInt(RESULT_INDEX, which);
                    getParentFragmentManager().setFragmentResult(requestKey, result);
                })
                .setNegativeButton(R.string.action_cancel, null)
                .create();
    }
}
