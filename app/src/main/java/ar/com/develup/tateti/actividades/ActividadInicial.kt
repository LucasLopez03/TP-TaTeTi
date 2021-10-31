package ar.com.develup.tateti.actividades

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import ar.com.develup.tateti.R
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.ktx.auth
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfig
import kotlinx.android.synthetic.main.actividad_inicial.*
import java.lang.RuntimeException
import java.util.HashMap

class ActividadInicial : AppCompatActivity() {

    private lateinit var firebaseAnalytics: FirebaseAnalytics
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firebaseRemoteConfig: FirebaseRemoteConfig

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.actividad_inicial)
        firebaseAnalytics = Firebase.analytics
        firebaseAuth = Firebase.auth
        firebaseRemoteConfig = Firebase.remoteConfig

        iniciarSesion.setOnClickListener { iniciarSesion() }
        registrate.setOnClickListener { registrate() }
        olvideMiContrasena.setOnClickListener { olvideMiContrasena() }

        if (usuarioEstaLogueado()) {
            // Si el usuario esta logueado, se redirige a la pantalla
            // de partidas
            verPartidas()
            finish()
        }
        actualizarRemoteConfig()
    }

    private fun usuarioEstaLogueado(): Boolean {
        // TODO-05-AUTHENTICATION
        // Validar que currentUser sea != null
//        if (FirebaseAuth.getInstance().currentUser != null){
//            return true
//        }
        if (firebaseAuth.currentUser != null){
            return true
        }
        return false
    }

    private fun verPartidas() {
        val intent = Intent(this, ActividadPartidas::class.java)
        startActivity(intent)
    }

    private fun registrate() {
        val intent = Intent(this, ActividadRegistracion::class.java)
        startActivity(intent)
    }

    private fun actualizarRemoteConfig() {
        configurarDefaultsRemoteConfig()
        configurarOlvideMiContrasena()
    }

    private fun configurarDefaultsRemoteConfig() {
        // TODO-04-REMOTECONFIG
        // Configurar los valores por default para remote config,
        // ya sea por codigo o por XML
        val defaults: MutableMap<String, Any> = HashMap()
        defaults["olvideMiContrasena"] = true
        firebaseRemoteConfig.setDefaultsAsync(defaults)
    }

    private fun configurarOlvideMiContrasena() {
        // TODO-04-REMOTECONFIG
        // Obtener el valor de la configuracion para saber si mostrar
        // o no el boton de olvide mi contraseña

        val featureActivo = firebaseRemoteConfig.getBoolean("olvideMiContrasena")
        val visibilidad = if (featureActivo) View.VISIBLE else View.GONE
        olvideMiContrasena!!.visibility = visibilidad
    }

    private fun olvideMiContrasena() {
        // Obtengo el mail
        val email = email.text.toString()

        // Si no completo el email, muestro mensaje de error
        if (email.isEmpty()) {
            Snackbar.make(rootView!!, "Completa el email", Snackbar.LENGTH_SHORT).show()
        } else {
            // TODO-05-AUTHENTICATION
            // Si completo el mail debo enviar un mail de reset
            // Para ello, utilizamos sendPasswordResetEmail con el email como parametro
            // Agregar el siguiente fragmento de codigo como CompleteListener, que notifica al usuario
            // el resultado de la operacion

           firebaseAuth.sendPasswordResetEmail(email)
              .addOnCompleteListener { task ->
                  if (task.isSuccessful) {
                      Snackbar.make(rootView, "Email enviado", Snackbar.LENGTH_SHORT).show()
                  } else {
                      Snackbar.make(rootView, "Error " + task.exception, Snackbar.LENGTH_SHORT).show()
                  }
              }
        }
    }

    private fun iniciarSesion() {
        val email = email.text.toString()
        val password = password.text.toString()
        //Integrar Crashlytics. Clickear en “Iniciar sesión” con los campos vacíos,
        // ver que la app tenga un crash, verificar en la consola que el crash figure y
        // encontrar la forma de solucionarlo.

//        if(email.isEmpty() || password.isEmpty()){
//            throw RuntimeException("Campos vacios de inicio de sesion")
//        }

        // TODO-05-AUTHENTICATION
        // hacer signInWithEmailAndPassword con los valores ingresados de email y password
        // Agregar en addOnCompleteListener el campo authenticationListener definido mas abajo
        if (email.isEmpty()){
            Snackbar.make(rootView!!, "Completa el mail", Snackbar.LENGTH_SHORT).show()
        }else{
            firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(authenticationListener)
        }
    }

        private val authenticationListener: OnCompleteListener<AuthResult?> = OnCompleteListener<AuthResult?> { task ->
            if (task.isSuccessful) {
                if (usuarioVerificoEmail()) {
                    verPartidas()
                } else {
                    desloguearse()
                    Snackbar.make(rootView!!, "Verifica tu email para continuar", Snackbar.LENGTH_SHORT).show()
                }
            } else {
                if (task.exception is FirebaseAuthInvalidUserException) {
                    Snackbar.make(rootView!!, "El usuario no existe", Snackbar.LENGTH_SHORT).show()
                } else if (task.exception is FirebaseAuthInvalidCredentialsException) {
                    Snackbar.make(rootView!!, "Credenciales inválidas", Snackbar.LENGTH_SHORT).show()
                }
            }
        }

    private fun usuarioVerificoEmail(): Boolean {
        // TODO-05-AUTHENTICATION
        // Preguntar al currentUser si verifico email
        if(firebaseAuth.currentUser?.isEmailVerified == true){
            return true
        }
        return false
    }

    private fun desloguearse() {
        // TODO-05-AUTHENTICATION
        // Hacer signOut de Firebase
        firebaseAuth.signOut()
    }
}