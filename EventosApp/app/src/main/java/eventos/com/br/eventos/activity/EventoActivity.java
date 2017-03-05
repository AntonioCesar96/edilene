package eventos.com.br.eventos.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
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
    private boolean flagClickFab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_evento);
        setUpToolbar();

        initFields();

        e = (Evento) getIntent().getSerializableExtra("evento");

        //getSupportActionBar().setTitle(e.getNome());
        setTitle(e.getNome());
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // Imagem de header na action bar
        ImageUtils.setImageEventoIndividual(getActivity(), e.getEnderecoImagem(), appBarImg, progressBar, collapsingToolbar);
        appBarImg.setOnClickListener(clickImg());

        fabFavorito.setOnClickListener(clickFabFavorito());

        // Adiciona o fragment no layout
        if (savedInstanceState == null) {
            // Cria o fragment com o mesmo Bundle (args) da intent
            EventoFragment frag = new EventoFragment();
            frag.setArguments(getIntent().getExtras());
            // Adiciona o fragment no layout
            getSupportFragmentManager().beginTransaction().add(R.id.eventoFragment, frag).commit();
        }
    }

    private View.OnClickListener clickFabFavorito() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (flagClickFab) {
                    fabFavorito.setImageResource(R.drawable.ic_turned_in_not_not_padding);
                    flagClickFab = false;
                    return;
                }
                fabFavorito.setImageResource(R.drawable.ic_turned_in_not_padding);
                flagClickFab = true;
            }
        };
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
                ActivityCompat.finishAfterTransition(getActivity());
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
