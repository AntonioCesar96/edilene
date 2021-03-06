package eventos.com.br.eventos.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;

import eventos.com.br.eventos.R;
import eventos.com.br.eventos.fragments.EventoFragment;
import eventos.com.br.eventos.model.Evento;
import eventos.com.br.eventos.util.ImageUtils;

public class EventoActivity extends BaseActivity {

    private CollapsingToolbarLayout collapsingToolbar;
    private ProgressBar progressBar;
    private FloatingActionButton fabFavorito;
    private ImageView appBarImg;
    private Evento e;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_evento);
        setUpToolbar();

        initFields();

        e = (Evento) getIntent().getSerializableExtra("evento");

        configToolbar();

        // Imagem de header na action bar
        ImageUtils.setImageEventoIndividual(getActivity(), e.getEnderecoImagem(), appBarImg, progressBar, collapsingToolbar);

        configClicks();

        addFragment(savedInstanceState);
    }

    private void addFragment(Bundle savedInstanceState) {
        // Adiciona o fragment no layout
        if (savedInstanceState == null) {
            // Cria o fragment com o mesmo Bundle (args) da intent
            EventoFragment frag = new EventoFragment();
            frag.setFabButton(fabFavorito);
            frag.setArguments(getIntent().getExtras());
            // Adiciona o fragment no layout
            getSupportFragmentManager().beginTransaction().add(R.id.eventoFragment, frag).commit();
        }
    }

    private void configClicks() {
        appBarImg.setOnClickListener(clickImg());
    }

    private void configToolbar() {
        setTitle(e.getNome());
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    private View.OnClickListener clickImg() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext(), PhotoViewActivity.class);
                intent.putExtra("url", e.getEnderecoImagem());
                startActivity(intent);
            }
        };
    }

    private void initFields() {
        collapsingToolbar = (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar);
        progressBar = (ProgressBar) findViewById(R.id.progressImg);
        fabFavorito = (FloatingActionButton) findViewById(R.id.fabFavorito);
        appBarImg = (ImageView) findViewById(R.id.appBarImg);
    }

    public void setTitle(String s) {
        // O título deve ser setado na CollapsingToolbarLayout
        collapsingToolbar.setTitle(s);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                boolean notificacao = getIntent().getBooleanExtra("notificacao", false);
                if (notificacao) {
                    startActivity(new Intent(getContext(), MainActivity.class));
                    finish();
                }

        }
        return super.onOptionsItemSelected(item);
    }
}
