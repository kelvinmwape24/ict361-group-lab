package zm.mu.ict361lab.ui.common;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;

import zm.mu.ict361lab.R;

/**
 * Ask for one value, and keep what was typed across a rotation.
 *
 * The EditText carries an id, so the platform saves and restores its contents
 * with the rest of the fragment's view state. Typing half a student number and
 * turning the phone no longer loses it.
 */
public class InputDialogFragment extends DialogFragment {

    public static final String RESULT_VALUE = "value";

    private static final String ARG_REQUEST = "request";
    private static final String ARG_TITLE   = "title";
    private static final String ARG_MESSAGE = "message";
    private static final String ARG_HINT    = "hint";

    public static void show(@NonNull FragmentManager manager, @NonNull String requestKey,
                            int titleRes, int messageRes, int hintRes) {
        InputDialogFragment f = new InputDialogFragment();
        Bundle args = new Bundle();
        args.putString(ARG_REQUEST, requestKey);
        args.putInt(ARG_TITLE, titleRes);
        args.putInt(ARG_MESSAGE, messageRes);
        args.putInt(ARG_HINT, hintRes);
        f.setArguments(args);
        f.show(manager, requestKey);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Bundle args = requireArguments();
        final String requestKey = args.getString(ARG_REQUEST, "input");

        View view = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_input, null, false);
        final EditText input = view.findViewById(R.id.dialog_input_field);
        input.setHint(args.getInt(ARG_HINT));

        return new AlertDialog.Builder(requireContext())
                .setTitle(args.getInt(ARG_TITLE))
                .setMessage(args.getInt(ARG_MESSAGE))
                .setView(view)
                .setPositiveButton(R.string.action_save, (d, w) -> {
                    Bundle result = new Bundle();
                    result.putString(RESULT_VALUE, input.getText().toString());
                    getParentFragmentManager().setFragmentResult(requestKey, result);
                })
                .setNegativeButton(R.string.action_cancel, null)
                .create();
    }
}
