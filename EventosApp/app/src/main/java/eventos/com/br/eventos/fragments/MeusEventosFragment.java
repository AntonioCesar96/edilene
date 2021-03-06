package eventos.com.br.eventos.fragments;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.ListPopupWindow;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListAdapter;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.List;

import eventos.com.br.eventos.R;
import eventos.com.br.eventos.activity.EventoActivity;
import eventos.com.br.eventos.activity.EventoAtualizarActivity;
import eventos.com.br.eventos.adapter.MeusEventoAdapter;
import eventos.com.br.eventos.model.Evento;
import eventos.com.br.eventos.model.Response;
import eventos.com.br.eventos.rest.EventoRest;
import eventos.com.br.eventos.tasks.BuscarEventoTask;
import eventos.com.br.eventos.tasks.EventoExcluirTask;
import eventos.com.br.eventos.util.TipoBusca;

public class MeusEventosFragment extends BaseFragment {
    protected RecyclerView recyclerView;
    private List<Evento> eventos;
    private TipoBusca tipoDeBusca;
    private ProgressBar progress;
    public static final int RECARREGAR_EVENTOS = 5;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.tipoDeBusca = TipoBusca.USUARIO;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_meus_eventos, container, false);

        progress = (ProgressBar) view.findViewById(R.id.progress);

        recyclerView = (RecyclerView) view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setHasFixedSize(true);

        return view;
    }

    @Override
    public void onResume() {
        if (TipoBusca.FAVORITOS.equals(tipoDeBusca)) {
            taskEventos();
        }

        super.onResume();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        taskEventos();
    }

    public void taskEventos() {
        // Busca os eventos: Dispara a Task
        //startTask("eventos", new GetEventosTask(pullToRefresh, getContext()), pullToRefresh ? R.id.swipeToRefresh : R.id.progress);
        new GetEventosTask().execute();
    }

    private class GetEventosTask extends AsyncTask<Void, Void, List<Evento>> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progress.setVisibility(View.VISIBLE);
        }

        @Override
        protected List<Evento> doInBackground(Void... longs) {
            EventoRest service = new EventoRest(getContext());

            try {
                return service.getEventos(tipoDeBusca);
            } catch (Exception e) {
                return null;
            }
        }

        @Override
        protected void onPostExecute(List<Evento> eventos) {
            progress.setVisibility(View.GONE);

            if (eventos != null) {
                // Salva a lista de eventos no atributo da classe
                MeusEventosFragment.this.eventos = eventos;
                // Atualiza a view na UI Thread
                recyclerView.setAdapter(new MeusEventoAdapter(getContext(), eventos, onClickEvento(), onClickEditar()));
                return;
            }

            snack(recyclerView, "Ocorreu algum erro ao buscar os dados.");
        }
    }

    private MeusEventoAdapter.EditarOnClickListener onClickEditar() {
        return new MeusEventoAdapter.EditarOnClickListener() {
            @Override
            public void onClickEditar(View view, int idx) {
                Evento e = eventos.get(idx);
                mostraOpcoes(view, e);
                //showListPopup(view);
            }
        };
    }

    private MeusEventoAdapter.EventoOnClickListener onClickEvento() {
        return new MeusEventoAdapter.EventoOnClickListener() {
            @Override
            public void onClickEvento(MeusEventoAdapter.EventoViewHolder holder, int idx) {
                Evento e = eventos.get(idx);

                Intent intent = new Intent(getContext(), EventoActivity.class);
                intent.putExtra("evento", e);
                startActivity(intent);
            }
        };
    }

    private void mostraOpcoes(View ancoraView, final Evento evento) {
        Context themeContext = getAppCompatActivity().getSupportActionBar().getThemedContext();
        if (ancoraView != null && themeContext != null) {

            PopupMenu popupMenu = new PopupMenu(themeContext, ancoraView);

            MenuItem m1 = popupMenu.getMenu().add(0, 0, 0, "Excluir");
            m1.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
            m1.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem menuItem) {

                    abrirDialogexcluir(evento);
                    return true;
                }
            });

            MenuItem m2 = popupMenu.getMenu().add(0, 0, 0, "Editar");
            m2.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
            m2.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem menuItem) {

                    new BuscarEventoTask(getAppCompatActivity(), onCallbackBuscarEvento()).execute(evento.getId());
                    return true;
                }
            });

            popupMenu.show();
        }
    }

    private void abrirDialogexcluir(final Evento evento) {
        final android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(getContext());
        builder.setTitle("Alerta");
        builder.setMessage("Deseja deletar este evento?");

        builder.setPositiveButton("Sim", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                try {
                    new EventoExcluirTask(getAppCompatActivity(), onCallbackExcluirEvento()).execute(evento.getId());
                } catch (Exception e) {
                    Log.i("ERRO", e.getMessage());
                }

                dialogInterface.dismiss();
            }
        });

        builder.setNegativeButton("Não", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.cancel();
            }
        });

        android.support.v7.app.AlertDialog dialog = builder.create();
        dialog.show();
    }

    private EventoExcluirTask.CallbackExcluirEvento onCallbackExcluirEvento() {
        return new EventoExcluirTask.CallbackExcluirEvento() {
            @Override
            public void onCallbackExcluirEvento(Response response) {
                if (response == null) {
                    response = new Response();
                    response.setMsg("Aconteceu algum erro inesperado!");
                    response.setStatus("Erro");
                }

                final android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(getContext());
                builder.setTitle("Alerta");
                builder.setMessage(response.getMsg());

                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });

                android.support.v7.app.AlertDialog dialog = builder.create();
                dialog.show();

                taskEventos();
            }
        };
    }

    private BuscarEventoTask.CallbackBuscarEvento onCallbackBuscarEvento() {
        return new BuscarEventoTask.CallbackBuscarEvento() {
            @Override
            public void onCallbackBuscarEvento(Evento e) {
                if (e != null) {
                    Intent intent = new Intent(getAppCompatActivity(), EventoAtualizarActivity.class);
                    intent.putExtra("evento", e);
                    startActivityForResult(intent, RECARREGAR_EVENTOS);
                }
            }
        };
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RECARREGAR_EVENTOS) {
            taskEventos();
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    public void showListPopup(View anchor) {
        String[] list = new String[]{"one", "two", "three"};

        ListPopupWindow popup = new ListPopupWindow(getAppCompatActivity());
        popup.setAnchorView(anchor);

        popup.setWidth(200);

        ListAdapter adapter = new MyAdapter(getAppCompatActivity(), list);
        popup.setAdapter(adapter);
        popup.show();
    }

    public static class MyAdapter extends BaseAdapter {

        private AppCompatActivity activity;
        private String[] list;

        public MyAdapter(AppCompatActivity activity, String[] list) {
            this.activity = activity;
            this.list = list;
        }

        @Override
        public int getCount() {
            return list.length;
        }

        @Override
        public Object getItem(int position) {
            return list[position];
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            TextView text = null;
            if (convertView == null) {
                convertView = LayoutInflater.from(activity).inflate(R.layout.adapter_popup_menu, null);
                text = (TextView) convertView.findViewById(R.id.text);
            } else {
                text = (TextView) convertView.findViewById(R.id.text);
            }

            text.setText(list[position]);
            return convertView;
        }
    }
}
