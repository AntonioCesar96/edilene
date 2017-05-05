package eventos.com.br.eventos.activity;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TimePicker;

import java.io.File;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import eventos.com.br.eventos.R;
import eventos.com.br.eventos.adapter.CidadesAdapter;
import eventos.com.br.eventos.adapter.EstadosAdapter;
import eventos.com.br.eventos.adapter.FaculdadesAdapter;
import eventos.com.br.eventos.config.EventosApplication;
import eventos.com.br.eventos.dao.EventoRascunhoDAO;
import eventos.com.br.eventos.model.Cidade;
import eventos.com.br.eventos.model.Estado;
import eventos.com.br.eventos.model.Evento;
import eventos.com.br.eventos.model.EventoRascunho;
import eventos.com.br.eventos.model.Faculdade;
import eventos.com.br.eventos.model.Local;
import eventos.com.br.eventos.model.Localizacao;
import eventos.com.br.eventos.model.Response;
import eventos.com.br.eventos.model.Usuario;
import eventos.com.br.eventos.tasks.CidadeTask;
import eventos.com.br.eventos.tasks.DownloadImagemTask;
import eventos.com.br.eventos.tasks.EstadoTask;
import eventos.com.br.eventos.tasks.EventoTask;
import eventos.com.br.eventos.tasks.FaculdadeTask;
import eventos.com.br.eventos.tasks.LocalizacaoTask;
import eventos.com.br.eventos.util.AlertUtils;
import eventos.com.br.eventos.util.AndroidUtils;
import eventos.com.br.eventos.util.CameraUtil;
import eventos.com.br.eventos.util.ValidationUtil;

public class EventoAtualizarActivity extends BaseActivity {
    private EditText txtNome, txtDesc, txtDataInicio, txtHoraInicio, txtLocalNome, txtLocalCep, txtLocalRua, txtLocalBairro, txtLocalNumero, txtNomeAtletica;
    private List<EditText> editTexts;
    private ImageView imgView;
    private Evento evento;
    private Button btnSalvar;
    private Spinner spFaculdades, spEstados, spCidades;
    private Faculdade faculdadeSelecionada;
    private Estado estadoSelecionado;
    private Cidade cidadeSelecionada;
    private Localizacao localizacao;
    private File fileImage;
    private CameraUtil cameraUtil;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_evento_cadastro);
        setUpToolbar();
        setUpNavigation();

        cameraUtil = new CameraUtil(getActivity());
        evento = new Evento();

        initFields();

        // Botão salvar
        btnSalvar.setOnClickListener(onClickSalvar());

        // Botão abrir a galeria
        ImageButton btAbrirGaleria = (ImageButton) findViewById(R.id.btnGaleria);
        btAbrirGaleria.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cameraUtil.abrirGaleria(getActivity());
            }
        });

        buscar();

        focusEditText();

        Intent intent = getIntent();
        if (intent != null) {
            Bundle bundle = intent.getExtras();
            if (bundle != null) {
                this.evento = (Evento) bundle.getSerializable("evento");
                if (evento != null) {
                    preencherCampos(evento);
                }
            }
        }

        setDateTimeField();
    }

    private void preencherCampos(Evento e) {

        if (e.getDataHora() != null) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            txtDataInicio.setText(dateFormat.format(e.getDataHora().getTime()));

            SimpleDateFormat hourFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
            txtHoraInicio.setText(hourFormat.format(e.getDataHora().getTime()));
        }

        txtNome.setText(e.getNome());
        txtNomeAtletica.setText(e.getNomeAtletica());
        txtDesc.setText(e.getDescricao());

        txtLocalNome.setText(e.getLocal().getNome());
        txtLocalRua.setText(e.getLocal().getRua());
        txtLocalBairro.setText(e.getLocal().getBairro());
        txtLocalNumero.setText(e.getLocal().getNumero());

        txtLocalCep.setText(e.getLocal().getCep());

        new DownloadImagemTask(getAppCompatActivity(), onCallbackDownloadImagem()).execute(e.getEnderecoImagem());
    }

    public DownloadImagemTask.CallbackDownloadImagem onCallbackDownloadImagem(){
        return new DownloadImagemTask.CallbackDownloadImagem() {
            @Override
            public void onCallbackDownloadImagem(File imagem) {

                fileImage = cameraUtil.setImage(imagem.getAbsolutePath(), imgView);
            }
        };
    }

    public void focusEditText() {
        final View viewNome = findViewById(R.id.viewNome);

        txtNome.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if (hasFocus) {
                    viewNome.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.primary));
                } else {
                    viewNome.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.cinzaBBB));
                }
            }
        });
    }

    private void initFields() {
        txtNome = (EditText) findViewById(R.id.tNome);
        txtDesc = (EditText) findViewById(R.id.tDesc);
        txtDataInicio = (EditText) findViewById(R.id.tDataInicio);
        txtHoraInicio = (EditText) findViewById(R.id.tHoraInicio);
        txtLocalNome = (EditText) findViewById(R.id.localNome);
        txtLocalCep = (EditText) findViewById(R.id.localCep);
        txtLocalRua = (EditText) findViewById(R.id.localRua);
        txtLocalBairro = (EditText) findViewById(R.id.localBairro);
        txtLocalNumero = (EditText) findViewById(R.id.localNumero);
        txtNomeAtletica = (EditText) findViewById(R.id.tNomeAtletica);
        imgView = (ImageView) findViewById(R.id.imgGaleria);
        spFaculdades = (Spinner) findViewById(R.id.spFaculdades);
        spEstados = (Spinner) findViewById(R.id.spLocalEstado);
        spCidades = (Spinner) findViewById(R.id.spLocalCidade);
        btnSalvar = (Button) findViewById(R.id.btSalvar);
        editTexts = Arrays.asList(txtNome, txtDesc, txtDataInicio, txtHoraInicio, txtLocalNome, txtLocalCep, txtLocalRua, txtLocalBairro, txtLocalNumero, txtNomeAtletica);

        txtLocalCep.addTextChangedListener(LocalCepTextChangedListener());
    }

    private TextWatcher LocalCepTextChangedListener() {
        return new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String padrao1 = "^\\d{5}[-]\\d{3}$";
                String padrao2 = "^\\d{5}\\d{3}$";

                String cep = s.toString();

                if (cep.matches(padrao1) || cep.matches(padrao2)) {
                    new LocalizacaoTask(getActivity(), onCallbackSearchLocation()).execute(cep);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        };
    }

    private LocalizacaoTask.CallbackSearchLocation onCallbackSearchLocation() {
        return new LocalizacaoTask.CallbackSearchLocation() {
            @Override
            public void onCallbackSearchLocation(Localizacao localizacao) {

                EventoAtualizarActivity.this.localizacao = localizacao;

                txtLocalRua.setText(localizacao.getLogradouro());
                txtLocalBairro.setText(localizacao.getBairro());

                BaseAdapter adapterEstados = (BaseAdapter) spEstados.getAdapter();

                for (int position = 0; position < adapterEstados.getCount(); position++) {
                    Estado estado = (Estado) adapterEstados.getItem(position);

                    if (estado.getUf() != null && estado.getUf().equals(localizacao.getUf())) {
                        spEstados.setSelection(position);
                    }
                }
            }
        };
    }

    private void selecionarCidade() {
        BaseAdapter adapterCidades = (BaseAdapter) spCidades.getAdapter();
        for (int position = 0; position < adapterCidades.getCount(); position++) {
            Cidade cidade = (Cidade) adapterCidades.getItem(position);

            if (cidade.getNome() != null && cidade.getNome().equals(localizacao.getLocalidade())) {
                spCidades.setSelection(position);
            }
        }

        localizacao = null;
    }

    private void selecionarFaculdade() {
        BaseAdapter adapterspFaculdades = (BaseAdapter) spFaculdades.getAdapter();
        for (int position = 0; position < adapterspFaculdades.getCount(); position++) {
            Faculdade f = (Faculdade) adapterspFaculdades.getItem(position);

            if (evento.getFaculdade() != null && f.getId().equals(evento.getFaculdade().getId())) {
                spFaculdades.setSelection(position);
            }
        }

        localizacao = null;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        this.fileImage = cameraUtil.pegarImagem(requestCode, resultCode, data, imgView);
    }

    private View.OnClickListener onClickSalvar() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                boolean validaOk = validaCampos();

                Usuario usuario = EventosApplication.getInstance().getUsuario();
                if (usuario == null) {
                    validaOk = false;
                }

                if (validaOk) {
                    // Validação de campos preenchidos
                    Local local = evento.getLocal();
                    local.setNome(txtLocalNome.getText().toString());
                    local.setCep(txtLocalCep.getText().toString());
                    local.setCidade(cidadeSelecionada);
                    local.setRua(txtLocalRua.getText().toString());
                    local.setBairro(txtLocalBairro.getText().toString());
                    local.setNumero(txtLocalNumero.getText().toString());

                    evento.setNome(txtNome.getText().toString());
                    evento.setDescricao(txtDesc.getText().toString());
                    evento.setNomeAtletica(txtNomeAtletica.getText().toString());
                    evento.setLocal(local);
                    evento.setFaculdade(faculdadeSelecionada);
                    evento.setUsuario(usuario);

                    salvarTask(evento);
                }
            }
        };
    }

    private boolean validaCampos() {
        List<EditText> editTexts = Arrays.asList(txtNome, txtDataInicio, txtHoraInicio, txtLocalNome, txtLocalCep,
                txtLocalRua, txtLocalBairro, txtLocalNumero, txtNomeAtletica, txtDesc);
        boolean validaOk = ValidationUtil.validateNotNull(editTexts);
        if (validaOk) {
            validaOk = ValidationUtil.validateName(txtNome);
        }
        if (validaOk) {
            validaOk = ValidationUtil.validateDate(txtDataInicio);
        }
        if (validaOk) {
            validaOk = ValidationUtil.validateTime(txtHoraInicio);
        }
        if (validaOk) {
            validaOk = ValidationUtil.validaSpinnerFaculdade(spFaculdades);
        }
        return validaOk;
    }

    private void salvarTask(Evento evento) {
        new EventoTask(fileImage, getActivity(), onCallbackSaveEvento()).execute(evento);
    }

    private EventoTask.CallbackSaveEvento onCallbackSaveEvento() {
        return new EventoTask.CallbackSaveEvento() {
            @Override
            public void onCallbackSaveEvento(Response response) {
                if (response != null && "OK".equals(response.getStatus())) {

                    try {
                        EventoRascunhoDAO dao = new EventoRascunhoDAO();
                        dao.removeAll();

                    } catch (SQLException e) {
                        Log.i("ERRO", e.getMessage());
                    }

                    setResult(MainActivity.RECRIAR_ACTIVITY);
                    finish();
                } else {
                    AlertUtils.alert(getAppCompatActivity(), "Alerta", "Erro ao tentar salvar evento " + evento.getNome());
                }
            }
        };
    }

    private void setDateTimeField() {

        Calendar calendar = evento.getDataHora();

        DatePickerDialog dataInicioDialog = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {

            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {

                SimpleDateFormat dateFormatter = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

                evento.getDataHora().set(year, monthOfYear, dayOfMonth);
                txtDataInicio.setText(dateFormatter.format(evento.getDataHora().getTime()));
            }

        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));

        TimePickerDialog horaInicioDialog = new TimePickerDialog(this, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {

                SimpleDateFormat timeFormatter = new SimpleDateFormat("HH:mm", Locale.getDefault());

                evento.getDataHora().set(Calendar.HOUR_OF_DAY, selectedHour);
                evento.getDataHora().set(Calendar.MINUTE, selectedMinute);

                txtHoraInicio.setText(timeFormatter.format(evento.getDataHora().getTime()));
            }
        }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true);

        dataInicioDialog.getDatePicker().setMinDate(Calendar.getInstance().getTime().getTime());

        txtDataInicio.setOnFocusChangeListener(focusGeneric(dataInicioDialog));
        txtDataInicio.setOnClickListener(clickGeneric(dataInicioDialog));

        txtHoraInicio.setOnFocusChangeListener(focusGeneric(horaInicioDialog));
        txtHoraInicio.setOnClickListener(clickGeneric(horaInicioDialog));
    }

    private View.OnClickListener clickGeneric(final AlertDialog dialog) {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.show();
            }
        };
    }

    private View.OnFocusChangeListener focusGeneric(final AlertDialog dialog) {
        return new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    dialog.show();
                }
            }
        };
    }

    private void buscar() {
        if (AndroidUtils.isNetworkAvailable(getContext())) {
            new EstadoTask(getActivity(), onCallbackSearchEstados()).execute();
        } else {
            alert("Alerta", "Você não esta conectado na internet");
        }
    }

    private FaculdadeTask.CallbackSearchFaculdades onCallbackSearchFaculdades() {
        return new FaculdadeTask.CallbackSearchFaculdades() {
            @Override
            public void onCallbackSearchFaculdades(List<Faculdade> faculdades) {
                if (faculdades == null) {
                    alert("Alerta", "Aconteceu algum erro ao tentar buscar as faculdades");
                    faculdades = new ArrayList<>();
                }

                // Label Faculdades
                criarLabelFaculdades(faculdades);

                BaseAdapter adapter = new FaculdadesAdapter(getContext(), faculdades);
                EventoAtualizarActivity.this.spFaculdades.setAdapter(adapter);
                EventoAtualizarActivity.this.spFaculdades.setSelection(0);

                final List<Faculdade> faculdadesAux = faculdades;

                EventoAtualizarActivity.this.spFaculdades.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                        faculdadeSelecionada = faculdadesAux.get(i);
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> adapterView) {

                    }
                });

                selecionarFaculdade();
            }
        };
    }

    private EstadoTask.CallbackSearchEstados onCallbackSearchEstados() {
        return new EstadoTask.CallbackSearchEstados() {
            @Override
            public void onCallbackSearchEstados(List<Estado> estados) {

                if (estados == null) {
                    alert("Alerta", "Aconteceu algum erro ao tentar buscar os estados");
                    estados = new ArrayList<>();
                }

                // Label Estado
                final Estado estado = new Estado();
                criarLabelEstado(estados, estado);

                BaseAdapter adapter = new EstadosAdapter(getContext(), estados);
                EventoAtualizarActivity.this.spEstados.setAdapter(adapter);
                EventoAtualizarActivity.this.spEstados.setSelection(0);

                final List<Estado> estadosAux = estados;

                EventoAtualizarActivity.this.spEstados.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                        estadoSelecionado = estadosAux.get(i);

                        if (estadoSelecionado.getId().equals(Long.MAX_VALUE)) {
                            return;
                        }
                        new CidadeTask(getActivity(), onCallbackSearchCidades()).execute(estadoSelecionado.getId());
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> adapterView) {

                    }
                });

                // Spinner cidades
                List<Cidade> cidades = new ArrayList<>();
                criarLabelCidade(cidades);

                BaseAdapter adapterCidades = new CidadesAdapter(getContext(), cidades);
                EventoAtualizarActivity.this.spCidades.setAdapter(adapterCidades);
                EventoAtualizarActivity.this.spCidades.setSelection(0);

                // Spinner Faculdades
                List<Faculdade> faculdades = new ArrayList<>();
                criarLabelFaculdades(faculdades);

                BaseAdapter adapterFaculdades = new FaculdadesAdapter(getContext(), faculdades);
                EventoAtualizarActivity.this.spFaculdades.setAdapter(adapterFaculdades);
                EventoAtualizarActivity.this.spFaculdades.setSelection(0);
            }
        };
    }

    private CidadeTask.CallbackSearchCidades onCallbackSearchCidades() {
        return new CidadeTask.CallbackSearchCidades() {
            @Override
            public void onCallbackSearchCidades(List<Cidade> cidades) {
                if (cidades == null) {
                    alert("Alerta", "Aconteceu algum erro ao tentar buscar as cidades");
                    cidades = new ArrayList<>();
                }

                // Label Cidade
                criarLabelCidade(cidades);

                CidadesAdapter adapter = (CidadesAdapter) EventoAtualizarActivity.this.spCidades.getAdapter();
                adapter.updateSpinner(cidades);
                EventoAtualizarActivity.this.spCidades.setSelection(0);

                final List<Cidade> cidadesAux = cidades;

                EventoAtualizarActivity.this.spCidades.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                        cidadeSelecionada = cidadesAux.get(i);

                        if (cidadeSelecionada.getId().equals(Long.MAX_VALUE)) {
                            return;
                        }

                        new FaculdadeTask(getActivity(), onCallbackSearchFaculdades()).execute(cidadeSelecionada.getId());
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> adapterView) {

                    }
                });

                if (localizacao != null) {
                    selecionarCidade();
                }
            }
        };
    }

    private void criarLabelEstado(List<Estado> estados, Estado estado) {
        estado.setNome("Estado");
        estado.setId(Long.MAX_VALUE);
        estados.add(0, estado);
    }

    private void criarLabelFaculdades(List<Faculdade> faculdades) {
        Faculdade f = new Faculdade();
        f.setNome("Selecione uma faculdade");
        f.setId(Long.MAX_VALUE);
        faculdades.add(0, f);
    }

    private void criarLabelCidade(List<Cidade> cidades) {
        Cidade cidade = new Cidade();
        cidade.setNome("Cidade");
        cidade.setId(Long.MAX_VALUE);
        cidades.add(0, cidade);
    }
}
