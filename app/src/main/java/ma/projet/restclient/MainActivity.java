package ma.projet.restclient;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import ma.projet.restclient.adapter.CompteAdapter;
import ma.projet.restclient.models.Compte;
import ma.projet.restclient.models.TypeCompte;
import ma.projet.restclient.network.RetrofitClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private CompteAdapter adapter;
    private FloatingActionButton fabAddCompte;
    private ProgressBar progressBar;
    private TextView tvEmptyState;
    private List<Compte> comptes = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();
        setupRecyclerView();
        loadComptes();

        fabAddCompte.setOnClickListener(v -> showAddEditDialog(null));
    }

    private void initViews() {
        recyclerView = findViewById(R.id.recyclerViewComptes);
        fabAddCompte = findViewById(R.id.fabAddCompte);
        progressBar = findViewById(R.id.progressBar);
        tvEmptyState = findViewById(R.id.tvEmptyState);
    }

    private void setupRecyclerView() {
        adapter = new CompteAdapter(
                comptes,
                this::showAddEditDialog,
                this::showDeleteConfirmation
        );
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }

    private void loadComptes() {
        showLoading(true);

        RetrofitClient.getApiService().getAllComptes().enqueue(new Callback<List<Compte>>() {
            @Override
            public void onResponse(Call<List<Compte>> call, Response<List<Compte>> response) {
                showLoading(false);
                if (response.isSuccessful() && response.body() != null) {
                    List<Compte> comptesList = response.body();
                    comptes.clear();
                    comptes.addAll(comptesList);
                    adapter.updateData(comptesList);
                    updateEmptyState();
                } else {
                    showToast("Erreur lors du chargement: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<List<Compte>> call, Throwable t) {
                showLoading(false);
                showToast("Erreur: " + t.getMessage());
                updateEmptyState();
            }
        });
    }

    private void showAddEditDialog(Compte compte) {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_compte, null);
        TextInputEditText etSolde = dialogView.findViewById(R.id.etSolde);
        RadioButton rbCourant = dialogView.findViewById(R.id.rbCourant);
        RadioButton rbEpargne = dialogView.findViewById(R.id.rbEpargne);
        TextView tvDialogTitle = dialogView.findViewById(R.id.tvDialogTitle);

        // Remplir les champs si c'est une édition
        if (compte != null) {
            tvDialogTitle.setText("Modifier le Compte");
            etSolde.setText(String.valueOf(compte.getSolde()));
            if (compte.getType() == TypeCompte.COURANT) {
                rbCourant.setChecked(true);
            } else {
                rbEpargne.setChecked(true);
            }
        }

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .create();

        dialogView.findViewById(R.id.btnCancel).setOnClickListener(v -> dialog.dismiss());

        dialogView.findViewById(R.id.btnSave).setOnClickListener(v -> {
            String soldeText = etSolde.getText() != null ? etSolde.getText().toString() : "";

            if (soldeText.isEmpty()) {
                etSolde.setError("Le solde est requis");
                return;
            }

            Double solde = null;
            try {
                solde = Double.parseDouble(soldeText);
            } catch (NumberFormatException e) {
                etSolde.setError("Solde invalide");
                return;
            }

            TypeCompte type = rbCourant.isChecked() ? TypeCompte.COURANT : TypeCompte.EPARGNE;

            if (compte == null) {
                createCompte(solde, type);
            } else {
                updateCompte(compte.getId(), solde, type);
            }

            dialog.dismiss();
        });

        dialog.show();
    }

    private void createCompte(double solde, TypeCompte type) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
        String currentDate = dateFormat.format(new Date());

        Compte newCompte = new Compte(solde, currentDate, type);

        showLoading(true);

        RetrofitClient.getApiService().createCompte(newCompte).enqueue(new Callback<Compte>() {
            @Override
            public void onResponse(Call<Compte> call, Response<Compte> response) {
                showLoading(false);
                if (response.isSuccessful()) {
                    showToast("Compte créé avec succès");
                    loadComptes();
                } else {
                    showToast("Erreur lors de la création: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<Compte> call, Throwable t) {
                showLoading(false);
                showToast("Erreur: " + t.getMessage());
            }
        });
    }

    private void updateCompte(Long id, double solde, TypeCompte type) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
        String currentDate = dateFormat.format(new Date());

        Compte updatedCompte = new Compte(id, solde, currentDate, type);

        showLoading(true);

        RetrofitClient.getApiService().updateCompte(id, updatedCompte).enqueue(new Callback<Compte>() {
            @Override
            public void onResponse(Call<Compte> call, Response<Compte> response) {
                showLoading(false);
                if (response.isSuccessful()) {
                    showToast("Compte modifié avec succès");
                    loadComptes();
                } else {
                    showToast("Erreur lors de la modification: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<Compte> call, Throwable t) {
                showLoading(false);
                showToast("Erreur: " + t.getMessage());
            }
        });
    }

    private void showDeleteConfirmation(Compte compte) {
        new AlertDialog.Builder(this)
                .setTitle("Confirmation")
                .setMessage("Voulez-vous vraiment supprimer ce compte ?")
                .setPositiveButton("Supprimer", (dialog, which) -> deleteCompte(compte))
                .setNegativeButton("Annuler", null)
                .show();
    }

    private void deleteCompte(Compte compte) {
        showLoading(true);

        RetrofitClient.getApiService().deleteCompte(compte.getId()).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                showLoading(false);
                if (response.isSuccessful()) {
                    showToast("Compte supprimé avec succès");
                    adapter.removeItem(compte);
                    updateEmptyState();
                } else {
                    showToast("Erreur lors de la suppression: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                showLoading(false);
                showToast("Erreur: " + t.getMessage());
            }
        });
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        recyclerView.setVisibility(show ? View.GONE : View.VISIBLE);
    }

    private void updateEmptyState() {
        tvEmptyState.setVisibility(comptes.isEmpty() ? View.VISIBLE : View.GONE);
        recyclerView.setVisibility(comptes.isEmpty() ? View.GONE : View.VISIBLE);
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}

