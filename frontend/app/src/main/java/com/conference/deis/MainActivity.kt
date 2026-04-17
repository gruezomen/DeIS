package com.conference.deis

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.conference.deis.network.RetrofitInstance
import com.conference.deis.network.model.LoginRequest
import com.conference.deis.network.model.RegisterRequest
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            DeISApp()
        }
    }
}

@Composable
fun DeISApp() {
    val navController = rememberNavController()

    MaterialTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            NavHost(
                navController = navController,
                startDestination = "splash"
            ) {
                composable("splash") {
                    SplashScreen(navController)
                }
                composable("login") {
                    LoginScreen(navController)
                }
                composable("register") {
                    RegisterScreen(navController)
                }
                composable("success") {
                    SuccessLoadingScreen(navController)
                }
                composable("home") {
                    AdminHomeScreen()
                }
            }
        }
    }
}

private val BlueBackground = Color(0xFF4D92E8)
private val FieldBackground = Color(0xFFF1F1F1)
private val CardColorBox = Color(0xFFB7A9A9)
private val ActionBoxColor = Color(0xFFD9D9D9)
private val LinkRed = Color(0xFFD60000)

@Composable
fun LogoSection() {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Image(
            painter = painterResource(id = R.drawable.delfin),
            contentDescription = "Logo Delfín",
            modifier = Modifier.size(140.dp)
        )

        Text(
            text = "DelIS",
            fontSize = 22.sp,
            color = Color.Black
        )
    }
}

@Composable
fun RegisterHeaderIcon() {
    Box(
        modifier = Modifier.size(100.dp),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.usuario),
            contentDescription = "Icono usuario",
            modifier = Modifier.size(90.dp)
        )
    }
}

@Composable
fun SplashScreen(navController: NavHostController) {
    LaunchedEffect(Unit) {
        delay(1800)
        navController.navigate("login") {
            popUpTo("splash") { inclusive = true }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BlueBackground),
        contentAlignment = Alignment.Center
    ) {
        LogoSection()
    }
}

@Composable
fun LoginScreen(navController: NavHostController) {
    var correo by remember { mutableStateOf("") }
    var contrasena by remember { mutableStateOf("") }
    var cargando by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BlueBackground)
            .padding(horizontal = 24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            LogoSection()

            Spacer(modifier = Modifier.height(24.dp))

            OutlinedTextField(
                value = correo,
                onValueChange = { correo = it },
                placeholder = { Text("Correo electrónico") },
                singleLine = true,
                shape = RoundedCornerShape(8.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = FieldBackground,
                    unfocusedContainerColor = FieldBackground,
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = contrasena,
                onValueChange = { contrasena = it },
                placeholder = { Text("Contraseña") },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                shape = RoundedCornerShape(8.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = FieldBackground,
                    unfocusedContainerColor = FieldBackground,
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(18.dp))

            Button(
                onClick = {
                    if (correo.isBlank() || contrasena.isBlank()) {
                        Toast.makeText(context, "Completa todos los campos", Toast.LENGTH_SHORT).show()
                        return@Button
                    }

                    scope.launch {
                        cargando = true
                        try {
                            val response = RetrofitInstance.api.iniciarSesion(
                                LoginRequest(
                                    correo = correo.trim(),
                                    contrasena = contrasena
                                )
                            )

                            if (response.isSuccessful && response.body() != null) {
                                Toast.makeText(
                                    context,
                                    response.body()!!.mensaje,
                                    Toast.LENGTH_SHORT
                                ).show()
                                navController.navigate("success")
                            } else {
                                Toast.makeText(
                                    context,
                                    "Correo o contraseña incorrectos",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        } catch (e: Exception) {
                            Toast.makeText(
                                context,
                                "No se pudo conectar al servidor",
                                Toast.LENGTH_SHORT
                            ).show()
                        } finally {
                            cargando = false
                        }
                    }
                },
                enabled = !cargando,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Black,
                    contentColor = Color.White
                )
            ) {
                if (cargando) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Iniciar Sesión")
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "Olvidé mi contraseña",
                color = Color.White,
                fontSize = 12.sp,
                modifier = Modifier.clickable { }
            )

            Spacer(modifier = Modifier.height(18.dp))

            Button(
                onClick = {
                    navController.navigate("register")
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor = Color.Black
                )
            ) {
                Text("Crear una cuenta")
            }
        }
    }
}

@Composable
fun RegisterScreen(navController: NavHostController) {
    var nombreCompleto by remember { mutableStateOf("") }
    var correoElectronico by remember { mutableStateOf("") }
    var contrasena by remember { mutableStateOf("") }
    var cargando by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BlueBackground)
            .padding(horizontal = 24.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 70.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            RegisterHeaderIcon()

            Spacer(modifier = Modifier.height(24.dp))

            OutlinedTextField(
                value = nombreCompleto,
                onValueChange = { nombreCompleto = it },
                placeholder = { Text("Nombre completo") },
                singleLine = true,
                shape = RoundedCornerShape(8.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = FieldBackground,
                    unfocusedContainerColor = FieldBackground,
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(22.dp))

            OutlinedTextField(
                value = correoElectronico,
                onValueChange = { correoElectronico = it },
                placeholder = { Text("Correo electronico") },
                singleLine = true,
                shape = RoundedCornerShape(8.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = FieldBackground,
                    unfocusedContainerColor = FieldBackground,
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(22.dp))

            OutlinedTextField(
                value = contrasena,
                onValueChange = { contrasena = it },
                placeholder = { Text("Contraseña") },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                shape = RoundedCornerShape(8.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = FieldBackground,
                    unfocusedContainerColor = FieldBackground,
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(18.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Tienes una cuenta? ",
                    color = Color.White,
                    fontSize = 14.sp
                )

                Text(
                    text = "Inicia Sesión",
                    color = LinkRed,
                    fontSize = 14.sp,
                    modifier = Modifier.clickable {
                        navController.navigate("login")
                    }
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            Button(
                onClick = {
                    if (nombreCompleto.isBlank() || correoElectronico.isBlank() || contrasena.isBlank()) {
                        Toast.makeText(context, "Completa todos los campos", Toast.LENGTH_SHORT).show()
                        return@Button
                    }

                    scope.launch {
                        cargando = true
                        try {
                            val response = RetrofitInstance.api.registrarUsuario(
                                RegisterRequest(
                                    nombre = nombreCompleto.trim(),
                                    correo = correoElectronico.trim(),
                                    contrasena = contrasena
                                )
                            )

                            if (response.isSuccessful && response.body() != null) {
                                Toast.makeText(
                                    context,
                                    response.body()!!.mensaje,
                                    Toast.LENGTH_SHORT
                                ).show()
                                navController.navigate("login")
                            } else {
                                Toast.makeText(
                                    context,
                                    "No se pudo registrar el usuario",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        } catch (e: Exception) {
                            Toast.makeText(
                                context,
                                "No se pudo conectar al servidor",
                                Toast.LENGTH_SHORT
                            ).show()
                        } finally {
                            cargando = false
                        }
                    }
                },
                enabled = !cargando,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Black,
                    contentColor = Color.White
                )
            ) {
                if (cargando) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(22.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Registrarse", fontSize = 20.sp)
                }
            }
        }
    }
}

@Composable
fun SuccessLoadingScreen(navController: NavHostController) {
    LaunchedEffect(Unit) {
        delay(1600)
        navController.navigate("home") {
            popUpTo("login") { inclusive = false }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BlueBackground),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Image(
                painter = painterResource(id = R.drawable.delfin),
                contentDescription = "Logo Delfín",
                modifier = Modifier.size(140.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Inicio de sesión exitoso!\nCargando...",
                fontSize = 12.sp,
                color = Color.Black
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminHomeScreen() {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("DelIS") },
                navigationIcon = {
                    Image(
                        painter = painterResource(id = R.drawable.delfin),
                        contentDescription = "Logo Delfín",
                        modifier = Modifier
                            .padding(start = 12.dp)
                            .size(32.dp)
                    )
                },
                actions = {
                    Box(
                        modifier = Modifier
                            .padding(end = 12.dp)
                            .size(34.dp)
                            .background(Color(0xFFE6E6E6), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("U")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = BlueBackground
                )
            )
        },
        bottomBar = {
            NavigationBar(containerColor = Color.White) {
                NavigationBarItem(
                    selected = true,
                    onClick = { },
                    icon = { },
                    label = { Text("Inicio") }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = { },
                    icon = { },
                    label = { Text("Simulacro") }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = { },
                    icon = { },
                    label = { Text("Banco") }
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color.White)
                .padding(16.dp)
        ) {
            Text(
                text = "Informacion",
                fontSize = 14.sp,
                color = Color.Black
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                InfoCard("20\npreguntas")
                InfoCard("3\nbancos")
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Qué quieres hacer?",
                fontSize = 14.sp,
                color = Color.Black
            )

            Spacer(modifier = Modifier.height(12.dp))

            ActionBox("Crear pregunta")
            Spacer(modifier = Modifier.height(10.dp))
            ActionBox("Ver lista de banco de preguntas")
        }
    }
}

@Composable
fun InfoCard(texto: String) {
    Card(
        modifier = Modifier.size(width = 90.dp, height = 72.dp),
        shape = RoundedCornerShape(6.dp),
        colors = CardDefaults.cardColors(containerColor = CardColorBox)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(10.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Text(
                text = texto,
                color = Color.White,
                fontSize = 12.sp
            )
        }
    }
}

@Composable
fun ActionBox(texto: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(ActionBoxColor)
            .padding(horizontal = 10.dp, vertical = 8.dp)
    ) {
        Text(
            text = texto,
            color = Color.Black,
            fontSize = 13.sp
        )
    }
}