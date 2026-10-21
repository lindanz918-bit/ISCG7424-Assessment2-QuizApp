package cs.linda.iscg7424assessment2quizapp.profile;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import cs.linda.iscg7424assessment2quizapp.login.LoginActivity;
import cs.linda.iscg7424assessment2quizapp.R;

public class ProfileFragment extends Fragment {

    private TextView tvEmail, tvRole;
    private EditText etFirstName, etLastName;
    private Button btnSave, btnChangePassword, btnLogout;
    private FirebaseAuth auth;
    private FirebaseFirestore db;

    public ProfileFragment() {

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        initUI(view);
        loadUserProfile();
        btnSave.setOnClickListener(v -> {
            String firstName = etFirstName.getText().toString().trim();
            String lastName = etLastName.getText().toString().trim();
            if (TextUtils.isEmpty(firstName) || TextUtils.isEmpty(lastName)) {
                Toast.makeText(requireContext(), "Please fill in all fields", Toast.LENGTH_SHORT).show();
                return;
            }
            FirebaseUser currentUser = auth.getCurrentUser();
            if (currentUser != null) {
                db.collection("users").document(currentUser.getUid())
                        .update("firstName", firstName, "lastName", lastName)
                        .addOnFailureListener(e ->
                                Toast.makeText(requireContext(), "Update failed: " + e.getMessage(), Toast.LENGTH_LONG).show());
                Toast.makeText(requireContext(), "Profile updated successfully!", Toast.LENGTH_SHORT).show();
            }
        });
        btnChangePassword.setOnClickListener(v -> showChangePasswordDialog());
        btnLogout.setOnClickListener(v -> performLogout());
    }

    private void initUI(View view) {
        etFirstName = view.findViewById(R.id.etFirstName);
        etLastName = view.findViewById(R.id.etLastName);
        tvEmail = view.findViewById(R.id.tvEmail);
        tvRole = view.findViewById(R.id.tvRole);
        btnSave = view.findViewById(R.id.btnSave);
        btnChangePassword = view.findViewById(R.id.btnChangePassword);
        btnLogout = view.findViewById(R.id.btnLogout);
    }

    private void loadUserProfile() {
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser != null) {
            String email = currentUser.getEmail();
            tvEmail.setText(email);

            db.collection("users").document(currentUser.getUid())
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            String firstName = documentSnapshot.getString("firstName");
                            String lastName = documentSnapshot.getString("lastName");
                            etFirstName.setText(firstName != null ? firstName : "N/A");
                            etLastName.setText(lastName != null ? lastName : "N/A");
                            String role = documentSnapshot.getString("role");
                            tvRole.setText(role != null ? role : "N/A");
                        }
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(requireContext(), "Failed to load profile: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                    );
        }
    }

    private void showChangePasswordDialog() {
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_change_password, null);
        TextInputEditText etNewPassword = dialogView.findViewById(R.id.etNewPassword);
        TextInputEditText etConfirmPassword = dialogView.findViewById(R.id.etConfirmPassword);

        new AlertDialog.Builder(requireContext())
                .setView(dialogView)
                .setPositiveButton("Update", (dialog, which) -> {
                    String newPass = etNewPassword.getText().toString().trim();
                    String confirmPass = etConfirmPassword.getText().toString().trim();

                    if (TextUtils.isEmpty(newPass) || newPass.length() < 6) {
                        Toast.makeText(requireContext(), "Password must be at least 6 characters", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (!newPass.equals(confirmPass)) {
                        Toast.makeText(requireContext(), "Passwords do not match", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    FirebaseUser user = auth.getCurrentUser();
                    if (user != null) {
                        user.updatePassword(newPass)
                                .addOnSuccessListener(unused ->
                                        Toast.makeText(requireContext(), "Password updated successfully!", Toast.LENGTH_SHORT).show()
                                )
                                .addOnFailureListener(e ->
                                        Toast.makeText(requireContext(), "Update failed: " + e.getMessage(), Toast.LENGTH_LONG).show()
                                );
                    }
                })
                .setNegativeButton("Cancel", null)
                .create()
                .show();
    }
    private void performLogout() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Logout")
                .setMessage("Are you sure you want to log out?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    auth.signOut();
                    Intent intent = new Intent(requireContext(), LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                })
                .setNegativeButton("No", null)
                .create()
                .show();
    }
}