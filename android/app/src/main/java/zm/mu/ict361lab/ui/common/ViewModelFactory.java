package zm.mu.ict361lab.ui.common;

import android.content.Context;
import android.os.Bundle;

import androidx.activity.ComponentActivity;
import androidx.annotation.NonNull;
import androidx.lifecycle.AbstractSavedStateViewModelFactory;
import androidx.lifecycle.SavedStateHandle;
import androidx.lifecycle.ViewModel;
import androidx.savedstate.SavedStateRegistryOwner;

import zm.mu.ict361lab.data.repository.AuthRepository;
import zm.mu.ict361lab.data.repository.RoomStudentRepository;
import zm.mu.ict361lab.data.repository.StudentRepository;
import zm.mu.ict361lab.ui.auth.AuthViewModel;
import zm.mu.ict361lab.ui.lecturer.EditorViewModel;
import zm.mu.ict361lab.ui.lecturer.RosterViewModel;
import zm.mu.ict361lab.ui.student.StudentViewModel;
import zm.mu.ict361lab.ui.sync.SyncViewModel;

/**
 * Hands each ViewModel its repository and its SavedStateHandle.
 *
 * Two jobs, and both matter:
 *
 *  1. It is the injection seam the unit tests use. Swap StudentRepository for a
 *     fake and every ViewModel runs with no device, no network and no database.
 *
 *  2. It extends AbstractSavedStateViewModelFactory rather than the plain
 *     Factory, which is what lets a ViewModel receive a SavedStateHandle. That
 *     handle is written into the Activity's saved instance state, so screen
 *     state survives the process being killed — not just a rotation. The brief
 *     is explicit that "ViewModel alone does not survive process death", and
 *     this is the line where that is dealt with.
 */
public class ViewModelFactory extends AbstractSavedStateViewModelFactory {

    private final StudentRepository students;
    private final AuthRepository auth;

    /** Production: the Activity is both the state owner and the context. */
    public ViewModelFactory(@NonNull ComponentActivity owner) {
        super(owner, null);
        Context app = owner.getApplicationContext();
        this.students = new RoomStudentRepository(app);
        this.auth = new AuthRepository(app);
    }

    /** Tests: inject a fake repository and, if needed, default arguments. */
    public ViewModelFactory(@NonNull SavedStateRegistryOwner owner, Bundle defaultArgs,
                            StudentRepository students, AuthRepository auth) {
        super(owner, defaultArgs);
        this.students = students;
        this.auth = auth;
    }

    @NonNull
    @Override
    @SuppressWarnings("unchecked")
    protected <T extends ViewModel> T create(@NonNull String key,
                                             @NonNull Class<T> modelClass,
                                             @NonNull SavedStateHandle handle) {
        if (modelClass.isAssignableFrom(AuthViewModel.class))
            return (T) new AuthViewModel(auth);
        if (modelClass.isAssignableFrom(RosterViewModel.class))
            return (T) new RosterViewModel(students, handle);
        if (modelClass.isAssignableFrom(StudentViewModel.class))
            return (T) new StudentViewModel(students, handle);
        if (modelClass.isAssignableFrom(EditorViewModel.class))
            return (T) new EditorViewModel(students, handle);
        if (modelClass.isAssignableFrom(SyncViewModel.class))
            return (T) new SyncViewModel(students);
        throw new IllegalArgumentException("Unknown ViewModel: " + modelClass.getName());
    }
}
