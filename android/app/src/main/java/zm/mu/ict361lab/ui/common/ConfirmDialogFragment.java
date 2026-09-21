package zm.mu.ict361lab.ui.common;

import android.app.Dialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;

/**
 * A yes/no dialog that survives rotation.
 *
 * The AlertDialog this replaces was built and shown straight from the Activity,
 * so a rotation while it was open destroyed it with no way back, and its
 * OnClickListener held a reference to an Activity that was already finished.
 *
 * A DialogFragment is recreated with its Activity, and the answer comes back
 * through setFragmentResult rather than a captured listener — so nothing
 * outlives the screen that asked the question.
 */
public class ConfirmDialogFragment extends DialogFragment {

    public static final String RESULT_CONFIRMED = "confirmed";

    private static final String ARG_REQUEST  = "request";
    private static final String ARG_TITLE    = "title";
    private static final String ARG_MESSAGE  = "message";
    private static final String ARG_POSITIVE = "positive";
    private static final String ARG_NEGATIVE = "negative";

    public static void show(@NonNull FragmentManager manager, @NonNull String requestKey,
                            int titleRes, int messageRes, int positiveRes, int negativeRes) {
        ConfirmDialogFragment f = new ConfirmDialogFragment();
        Bundle args = new Bundle();
        args.putString(ARG_REQUEST, requestKey);
        args.putInt(ARG_TITLE, titleRes);
        args.putInt(ARG_MESSAGE, messageRes);
        args.putInt(ARG_POSITIVE, positiveRes);
        args.putInt(ARG_NEGATIVE, negativeRes);
        f.setArguments(args);
        f.show(manager, requestKey);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Bundle args = requireArguments();
        final String requestKey = args.getString(ARG_REQUEST, "confirm");

        return new AlertDialog.Builder(requireContext())
                .setTitle(args.getInt(ARG_TITLE))
                .setMessage(args.getInt(ARG_MESSAGE))
                .setPositiveButton(args.getInt(ARG_POSITIVE), (d, w) -> deliver(requestKey, true))
                .setNegativeButton(args.getInt(ARG_NEGATIVE), (d, w) -> deliver(requestKey, false))
                .create();
    }

    private void deliver(String requestKey, boolean confirmed) {
        Bundle result = new Bundle();
        result.putBoolean(RESULT_CONFIRMED, confirmed);
        getParentFragmentManager().setFragmentResult(requestKey, result);
    }
}
