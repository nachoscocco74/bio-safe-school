package com.example.purabox;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private LinearLayout layoutInicio, layoutConexion, layoutImpacto, layoutInventario;
    private LinearLayout panelBluetooth, panelWiFi, bannerConexion;
    private LinearLayout layoutBadgeMaterial;

    private TextView textoTemporizador, textoEstadoCiclo, txtEstadoSub, txtBannerTitulo, txtBannerInfo, txtListaInventario;
    private TextView txtBadgeMaterial, txtAvisoManual;
    private EditText editNombreProducto;
    private Button botonIniciar, btnAccionConectar, btnModoBT, btnModoWiFi, btnConectarWiFi;
    private Button btnFast2, btnFast3, btnFast5, btnFast10, btnFast15, btnFast20;
    private Button btnDevBT1, btnDevBT2;
    private SeekBar seekBarTiempo;

    private TextView txtImpactoPlastico, txtImpactoEnvases, txtImpactoAhorro, txtImpactoCiclos, txtImpactoCO2;

    // Paleta de colores Premium Red & Black
    private final int COLOR_PRIMARY_RED   = Color.parseColor("#D32F2F");
    private final int COLOR_DARK_CARD     = Color.parseColor("#1C1C1E");
    private final int COLOR_DARK_GRAY_BG  = Color.parseColor("#2C2C2E");
    private final int COLOR_TEXT_MUTED    = Color.parseColor("#8E8E93");
    private final int COLOR_ALERT_RED     = Color.parseColor("#FF453A");
    private final int COLOR_STATUS_OK     = Color.parseColor("#30D158");

    // Estado del sistema
    private boolean dispositivoConectado = false;
    private int contadorCiclos = 0;
    private int gramosPlasticoEvitado = 0;
    private int dineroAhorrado = 0;
    private double co2Mitigado = 0.0;
    private String historialInventarioLog = "";

    private CountDownTimer temporizador;
    private boolean cicloIniciado = false;
    private int minutosSeleccionados = 5;
    private long tiempoRestanteEnMilisegundos = 5 * 60 * 1000L;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Vincular componentes UI
        layoutInicio      = findViewById(R.id.layoutInicio);
        layoutConexion    = findViewById(R.id.layoutConexion);
        layoutImpacto     = findViewById(R.id.layoutImpacto);
        layoutInventario  = findViewById(R.id.layoutInventario);
        panelBluetooth    = findViewById(R.id.panelBluetooth);
        panelWiFi         = findViewById(R.id.panelWiFi);
        bannerConexion    = findViewById(R.id.bannerConexion);
        layoutBadgeMaterial = findViewById(R.id.layoutBadgeMaterial);

        textoTemporizador  = findViewById(R.id.textoTemporizador);
        textoEstadoCiclo   = findViewById(R.id.textoEstadoCiclo);
        txtEstadoSub       = findViewById(R.id.txtEstadoSub);
        txtBannerTitulo    = findViewById(R.id.txtBannerTitulo);
        txtBannerInfo      = findViewById(R.id.txtBannerInfo);
        txtListaInventario = findViewById(R.id.txtListaInventario);
        txtBadgeMaterial   = findViewById(R.id.txtBadgeMaterial);
        txtAvisoManual     = findViewById(R.id.txtAvisoManual);
        editNombreProducto = findViewById(R.id.editNombreProducto);
        botonIniciar       = findViewById(R.id.botonIniciar);
        btnAccionConectar  = findViewById(R.id.btnAccionConectar);
        seekBarTiempo      = findViewById(R.id.seekBarTiempo);

        btnFast2   = findViewById(R.id.btnFast2);
        btnFast3   = findViewById(R.id.btnFast3);
        btnFast5   = findViewById(R.id.btnFast5);
        btnFast10  = findViewById(R.id.btnFast10);
        btnFast15  = findViewById(R.id.btnFast15);
        btnFast20  = findViewById(R.id.btnFast20);
        btnModoBT       = findViewById(R.id.btnModoBT);
        btnModoWiFi     = findViewById(R.id.btnModoWiFi);
        btnConectarWiFi = findViewById(R.id.btnConectarWiFi);
        btnDevBT1 = findViewById(R.id.btnDevBT1);
        btnDevBT2 = findViewById(R.id.btnDevBT2);

        txtImpactoPlastico = findViewById(R.id.txtImpactoPlastico);
        txtImpactoEnvases  = findViewById(R.id.txtImpactoEnvases);
        txtImpactoAhorro   = findViewById(R.id.txtImpactoAhorro);
        txtImpactoCiclos   = findViewById(R.id.txtImpactoCiclos);
        txtImpactoCO2      = findViewById(R.id.txtImpactoCO2);

        actualizarTableroVisual();

        // ── SeekBar: control manual de tiempo ──────────────────────────────
        seekBarTiempo.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (!fromUser) return;
                if (progress == 0) progress = 1;
                minutosSeleccionados = progress;
                tiempoRestanteEnMilisegundos = minutosSeleccionados * 60 * 1000L;
                actualizarTextoReloj();
                resaltarBotonActivo(-1); // ninguno resaltado si es ajuste manual
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        // ── Botones de tiempo rápido ────────────────────────────────────────
        btnFast2.setOnClickListener(v  -> cambiarMinutosRapido(2));
        btnFast3.setOnClickListener(v  -> cambiarMinutosRapido(3));
        btnFast5.setOnClickListener(v  -> cambiarMinutosRapido(5));
        btnFast10.setOnClickListener(v -> cambiarMinutosRapido(10));
        btnFast15.setOnClickListener(v -> cambiarMinutosRapido(15));
        btnFast20.setOnClickListener(v -> cambiarMinutosRapido(20));

        // ── Detección automática de material por nombre ─────────────────────
        editNombreProducto.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                if (!dispositivoConectado || cicloIniciado) return;
                String input = s.toString().trim();
                if (input.length() < 2) {
                    layoutBadgeMaterial.setVisibility(View.GONE);
                    txtAvisoManual.setText("");
                    return;
                }
                int[] resultado = detectarMaterial(input);
                int tiempoSugerido = resultado[0];
                int tipoMaterial   = resultado[1];

                if (tiempoSugerido > 0) {
                    cambiarMinutosRapido(tiempoSugerido);
                    layoutBadgeMaterial.setVisibility(View.VISIBLE);
                    txtBadgeMaterial.setText(etiquetaMaterial(tipoMaterial));
                    txtAvisoManual.setText("");
                    textoEstadoCiclo.setText("MATERIAL DETECTADO · TIEMPO AJUSTADO AUTOMÁTICAMENTE");
                    textoEstadoCiclo.setTextColor(COLOR_STATUS_OK);
                } else {
                    layoutBadgeMaterial.setVisibility(View.GONE);
                    txtAvisoManual.setText("⚠ MATERIAL NO IDENTIFICADO · AJUSTE EL TIEMPO MANUALMENTE");
                    textoEstadoCiclo.setText("MÓDULO CONFIGURADO. INGRESE ELEMENTO Y DE DISPARO.");
                    textoEstadoCiclo.setTextColor(COLOR_TEXT_MUTED);
                }
            }
        });

        // ── Botón iniciar/detener ciclo ─────────────────────────────────────
        botonIniciar.setOnClickListener(v -> {
            if (!dispositivoConectado) {
                Toast.makeText(this, "🛑 Acceso denegado: Hardware desconectado", Toast.LENGTH_SHORT).show();
                return;
            }
            if (!cicloIniciado) {
                comenzarCuentaRegresiva();
            } else {
                detenerCuentaRegresiva(false);
            }
        });

        // ── Selección de interfaz de comunicación ───────────────────────────
        btnModoBT.setOnClickListener(v -> {
            panelBluetooth.setVisibility(View.VISIBLE);
            panelWiFi.setVisibility(View.GONE);
            btnModoBT.setBackgroundTintList(ColorStateList.valueOf(COLOR_PRIMARY_RED));
            btnModoBT.setTextColor(Color.WHITE);
            btnModoWiFi.setBackgroundTintList(ColorStateList.valueOf(COLOR_DARK_CARD));
            btnModoWiFi.setTextColor(COLOR_TEXT_MUTED);
        });

        btnModoWiFi.setOnClickListener(v -> {
            panelBluetooth.setVisibility(View.GONE);
            panelWiFi.setVisibility(View.VISIBLE);
            btnModoWiFi.setBackgroundTintList(ColorStateList.valueOf(COLOR_PRIMARY_RED));
            btnModoWiFi.setTextColor(Color.WHITE);
            btnModoBT.setBackgroundTintList(ColorStateList.valueOf(COLOR_DARK_CARD));
            btnModoBT.setTextColor(COLOR_TEXT_MUTED);
        });

        // ── Handlers de enlace exitoso ──────────────────────────────────────
        View.OnClickListener listenerConectado = v -> registrarConexionExitosa();
        btnDevBT1.setOnClickListener(listenerConectado);
        btnDevBT2.setOnClickListener(listenerConectado);
        btnConectarWiFi.setOnClickListener(listenerConectado);
        btnAccionConectar.setOnClickListener(v -> irAPantalla(layoutConexion, findViewById(R.id.tabConexion)));

        // ── Navegación inferior ─────────────────────────────────────────────
        findViewById(R.id.tabInicio).setOnClickListener(v    -> irAPantalla(layoutInicio,     v));
        findViewById(R.id.tabConexion).setOnClickListener(v  -> irAPantalla(layoutConexion,   v));
        findViewById(R.id.tabImpacto).setOnClickListener(v   -> irAPantalla(layoutImpacto,    v));
        findViewById(R.id.tabInventario).setOnClickListener(v -> irAPantalla(layoutInventario, v));
    }

    // ════════════════════════════════════════════════════════════════
    //  DETECCIÓN AUTOMÁTICA DE MATERIAL
    //  Retorna int[]{tiempoMinutos, codigoTipoMaterial}
    //  codigoTipoMaterial: 0=no detectado, 1=plástico, 2=mixto,
    //                      3=textil, 4=vidrio, 5=metal
    // ════════════════════════════════════════════════════════════════
    private int[] detectarMaterial(String nombreProducto) {
        String n = nombreProducto.toLowerCase(Locale.getDefault());

        // ── Metal puro (10 minutos) ──────────────────────────────────
        if (n.contains("reloj")       || n.contains("tijera")      || n.contains("llave")     ||
                n.contains("metal")       || n.contains("acero")       || n.contains("inox")      ||
                n.contains("pinza")       || n.contains("herramienta") || n.contains("bisturi")   ||
                n.contains("bisturí")     || n.contains("navaja")      || n.contains("cuchillo")  ||
                n.contains("moneda")      || n.contains("llave")       || n.contains("tornillo")  ||
                n.contains("destornillador") || n.contains("clamp")    || n.contains("abrazadera")) {
            return new int[]{10, 5};
        }

        // ── Vidrio / cerámica (8 minutos) ────────────────────────────
        if (n.contains("vidrio")    || n.contains("vaso")      || n.contains("plato")    ||
                n.contains("ceramica") || n.contains("cerámica")  || n.contains("frasco")   ||
                n.contains("botella")  || n.contains("lente")     || n.contains("gafas")    ||
                n.contains("anteojos") || n.contains("copa")      || n.contains("tarro")) {
            return new int[]{8, 4};
        }

        // ── Textil / tela (7 minutos) ────────────────────────────────
        if (n.contains("tela")      || n.contains("ropa")       || n.contains("mascara")  ||
                n.contains("máscara")   || n.contains("tapaboca")   || n.contains("guante")   ||
                n.contains("trapo")     || n.contains("toalla")     || n.contains("paño")     ||
                n.contains("pano")      || n.contains("bufanda")    || n.contains("gorro")    ||
                n.contains("cubreboca") || n.contains("barbijo")) {
            return new int[]{7, 3};
        }

        // ── Plástico + metal mixto (5 minutos) ───────────────────────
        if (n.contains("teclado")   || n.contains("auricular")  || n.contains("joystick") ||
                n.contains("gamepad")   || n.contains("control")    || n.contains("calculadora") ||
                n.contains("tablet")    || n.contains("laptop")     || n.contains("notebook") ||
                n.contains("telefono")  || n.contains("teléfono")   || n.contains("celular")  ||
                n.contains("impresora") || n.contains("router")     || n.contains("modem")    ||
                n.contains("módem")     || n.contains("camara")     || n.contains("cámara")) {
            return new int[]{5, 2};
        }

        // ── Plástico puro (3 minutos) ────────────────────────────────
        if (n.contains("mouse")     || n.contains("raton")      || n.contains("ratón")    ||
                n.contains("pendrive")  || n.contains("usb")        || n.contains("plastico") ||
                n.contains("plástico")  || n.contains("estuche")    || n.contains("funda")    ||
                n.contains("lapicera")  || n.contains("boligrafo")  || n.contains("bolígrafo")||
                n.contains("regla")     || n.contains("carpeta")    || n.contains("porta")    ||
                n.contains("envase")    || n.contains("recipiente")) {
            return new int[]{3, 1};
        }

        return new int[]{-1, 0}; // No detectado
    }

    // ── Etiqueta de texto para el badge de material ──────────────────
    private String etiquetaMaterial(int tipo) {
        switch (tipo) {
            case 1: return "PLÁSTICO PURO · 3 MIN";
            case 2: return "PLÁSTICO + METAL · 5 MIN";
            case 3: return "TEXTIL / TELA · 7 MIN";
            case 4: return "VIDRIO / CERÁMICA · 8 MIN";
            case 5: return "METAL / ACERO · 10 MIN";
            default: return "DESCONOCIDO";
        }
    }

    // ════════════════════════════════════════════════════════════════
    //  CONTROL DE TIEMPO
    // ════════════════════════════════════════════════════════════════
    private void cambiarMinutosRapido(int mins) {
        if (!dispositivoConectado) return;
        minutosSeleccionados = mins;
        seekBarTiempo.setProgress(mins);
        tiempoRestanteEnMilisegundos = mins * 60 * 1000L;
        actualizarTextoReloj();
        resaltarBotonActivo(mins);
    }

    private void resaltarBotonActivo(int mins) {
        Button[] botones = {btnFast2, btnFast3, btnFast5, btnFast10, btnFast15, btnFast20};
        int[]    valores  = {2, 3, 5, 10, 15, 20};
        for (int i = 0; i < botones.length; i++) {
            if (valores[i] == mins) {
                botones[i].setBackgroundTintList(ColorStateList.valueOf(COLOR_PRIMARY_RED));
                botones[i].setTextColor(Color.WHITE);
            } else {
                botones[i].setBackgroundTintList(ColorStateList.valueOf(COLOR_DARK_GRAY_BG));
                botones[i].setTextColor(Color.WHITE);
            }
        }
    }

    private void actualizarTextoReloj() {
        int minutos  = (int) (tiempoRestanteEnMilisegundos / 1000) / 60;
        int segundos = (int) (tiempoRestanteEnMilisegundos / 1000) % 60;
        textoTemporizador.setText(String.format(Locale.getDefault(), "%02d:%02d", minutos, segundos));
    }

    // ════════════════════════════════════════════════════════════════
    //  CICLO DE ESTERILIZACIÓN
    // ════════════════════════════════════════════════════════════════
    private void comenzarCuentaRegresiva() {
        cicloIniciado = true;
        botonIniciar.setText("DETENER EMISIÓN (ABORTAR)");
        botonIniciar.setBackgroundTintList(ColorStateList.valueOf(COLOR_ALERT_RED));
        textoTemporizador.setTextColor(COLOR_PRIMARY_RED);
        textoEstadoCiclo.setText("RADIACIÓN GERMICIDA ACTIVA · UVC 254 nm");
        textoEstadoCiclo.setTextColor(COLOR_PRIMARY_RED);
        seekBarTiempo.setEnabled(false);
        editNombreProducto.setEnabled(false);
        layoutBadgeMaterial.setVisibility(View.GONE);

        temporizador = new CountDownTimer(tiempoRestanteEnMilisegundos, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                tiempoRestanteEnMilisegundos = millisUntilFinished;
                actualizarTextoReloj();
            }
            @Override
            public void onFinish() {
                detenerCuentaRegresiva(true);
            }
        }.start();
    }

    private void detenerCuentaRegresiva(boolean terminadoNatural) {
        cicloIniciado = false;
        if (temporizador != null) temporizador.cancel();

        botonIniciar.setText("INICIAR CICLO DE ESTERILIZACIÓN");
        botonIniciar.setBackgroundTintList(ColorStateList.valueOf(COLOR_PRIMARY_RED));
        seekBarTiempo.setEnabled(true);
        editNombreProducto.setEnabled(true);

        if (terminadoNatural) {
            contadorCiclos++;
            gramosPlasticoEvitado += 7;
            dineroAhorrado        += 35;
            co2Mitigado           += 0.01;

            String nombreProducto = editNombreProducto.getText().toString().trim();
            if (nombreProducto.isEmpty()) nombreProducto = "Elemento Indeterminado";

            String horaActual   = new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(new Date());
            String nuevoRegistro = " [OK] " + nombreProducto + "\n      -> Indexado a las " + horaActual + " hs\n\n";
            historialInventarioLog = nuevoRegistro + historialInventarioLog;
            txtListaInventario.setText(historialInventarioLog);

            actualizarTableroVisual();
            tiempoRestanteEnMilisegundos = minutosSeleccionados * 60 * 1000L;
            actualizarTextoReloj();

            textoTemporizador.setTextColor(COLOR_STATUS_OK);
            textoEstadoCiclo.setText("CICLO FINALIZADO · COOLING DOWN ACTIVO");
            textoEstadoCiclo.setTextColor(COLOR_STATUS_OK);
            Toast.makeText(this, "Protocolo completado con éxito.", Toast.LENGTH_SHORT).show();
        } else {
            textoTemporizador.setTextColor(COLOR_TEXT_MUTED);
            textoEstadoCiclo.setText("EMISIÓN INTERRUMPIDA POR EL OPERADOR");
            textoEstadoCiclo.setTextColor(COLOR_ALERT_RED);
            Toast.makeText(this, "Esterilización cancelada.", Toast.LENGTH_SHORT).show();
        }
    }

    // ════════════════════════════════════════════════════════════════
    //  CORTE AUTOMÁTICO POR SENSOR DE TAPA
    // ════════════════════════════════════════════════════════════════
    public void ejecutarCortePorTapaAbierta() {
        if (!cicloIniciado) return;
        cicloIniciado = false;
        if (temporizador != null) temporizador.cancel();

        botonIniciar.setText("INICIAR CICLO DE ESTERILIZACIÓN");
        botonIniciar.setBackgroundTintList(ColorStateList.valueOf(COLOR_PRIMARY_RED));
        seekBarTiempo.setEnabled(true);
        editNombreProducto.setEnabled(true);

        textoTemporizador.setTextColor(COLOR_ALERT_RED);
        textoEstadoCiclo.setText("🚨 ALERTA DE SEGURIDAD: TAPA DETECTADA ABIERTA. EMISIÓN INTERRUMPIDA.");
        textoEstadoCiclo.setTextColor(COLOR_ALERT_RED);
        Toast.makeText(this, "Corte de emergencia: Tapa abierta.", Toast.LENGTH_LONG).show();
    }

    // ════════════════════════════════════════════════════════════════
    //  ACTUALIZAR TABLERO DE IMPACTO
    // ════════════════════════════════════════════════════════════════
    private void actualizarTableroVisual() {
        txtImpactoCiclos.setText(String.valueOf(contadorCiclos));
        txtImpactoPlastico.setText(gramosPlasticoEvitado + " g");
        txtImpactoAhorro.setText("$" + dineroAhorrado);
        txtImpactoCO2.setText(String.format(Locale.getDefault(), "%.2f kg", co2Mitigado));
        int envasesEquivalentes = gramosPlasticoEvitado / 15;
        txtImpactoEnvases.setText("≈ " + envasesEquivalentes + " envases mitigados");
    }

    // ════════════════════════════════════════════════════════════════
    //  REGISTRAR CONEXIÓN EXITOSA
    // ════════════════════════════════════════════════════════════════
    private void registrarConexionExitosa() {
        dispositivoConectado = true;

        txtBannerTitulo.setText("HARDWARE LINKED SUCCESSFULLY");
        txtBannerTitulo.setTextColor(COLOR_STATUS_OK);
        txtBannerInfo.setText("Conexión serial establecida con el módulo PuraBox.");
        btnAccionConectar.setText("MODIFICAR");

        txtEstadoSub.setText("● SISTEMA ONLINE Y ENLACE VERIFICADO");
        txtEstadoSub.setTextColor(COLOR_STATUS_OK);
        textoTemporizador.setTextColor(COLOR_PRIMARY_RED);
        textoEstadoCiclo.setText("MÓDULO CONFIGURADO. INGRESE ELEMENTO Y DE DISPARO.");
        textoEstadoCiclo.setTextColor(COLOR_TEXT_MUTED);

        editNombreProducto.setEnabled(true);
        seekBarTiempo.setEnabled(true);

        Button[] botones = {btnFast2, btnFast3, btnFast5, btnFast10, btnFast15, btnFast20};
        for (Button b : botones) {
            b.setEnabled(true);
            b.setBackgroundTintList(ColorStateList.valueOf(COLOR_DARK_GRAY_BG));
            b.setTextColor(Color.WHITE);
        }
        // Resaltar 5 min por defecto
        btnFast5.setBackgroundTintList(ColorStateList.valueOf(COLOR_PRIMARY_RED));

        botonIniciar.setEnabled(true);
        botonIniciar.setText("INICIAR CICLO DE ESTERILIZACIÓN");
        botonIniciar.setBackgroundTintList(ColorStateList.valueOf(COLOR_PRIMARY_RED));
        botonIniciar.setTextColor(Color.WHITE);

        Toast.makeText(this, "Enlace de comunicación activo.", Toast.LENGTH_SHORT).show();
        irAPantalla(layoutInicio, findViewById(R.id.tabInicio));
    }

    // ════════════════════════════════════════════════════════════════
    //  NAVEGACIÓN
    // ════════════════════════════════════════════════════════════════
    private void irAPantalla(LinearLayout pantallaVisible, View tabPresionado) {
        layoutInicio.setVisibility(View.GONE);
        layoutConexion.setVisibility(View.GONE);
        layoutImpacto.setVisibility(View.GONE);
        layoutInventario.setVisibility(View.GONE);
        pantallaVisible.setVisibility(View.VISIBLE);

        LinearLayout nav = findViewById(R.id.barraNavegacionInferior);
        for (int i = 0; i < nav.getChildCount(); i++) {
            View child = nav.getChildAt(i);
            if (child instanceof LinearLayout) {
                TextView lbl = (TextView) ((LinearLayout) child).getChildAt(1);
                lbl.setTextColor(COLOR_TEXT_MUTED);
            }
        }
        TextView labelSeleccionado = (TextView) ((LinearLayout) tabPresionado).getChildAt(1);
        labelSeleccionado.setTextColor(Color.WHITE);
    }
}