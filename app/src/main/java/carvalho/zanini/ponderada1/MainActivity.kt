package carvalho.zanini.ponderada1

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import carvalho.zanini.ponderada1.ui.theme.Ponderada1Theme
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            LancadorDeDadosApp()
        }
    }
}

@Composable
fun DadoCanvas(tipo: String, valor: Int?, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.size(180.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val corFundo = Color(0xFFEFEFEF)
            val corContorno = Color(0xFF333333)
            val tracoLargura = 6f

            when (tipo) {
                "D6" -> {
                    val lado = minOf(w, h) * 0.85f
                    val origem = Offset((w - lado) / 2f, (h - lado) / 2f)
                    drawRect(
                        color = corFundo,
                        topLeft = origem,
                        size = Size(lado, lado)
                    )
                    drawRect(
                        color = corContorno,
                        topLeft = origem,
                        size = Size(lado, lado),
                        style = Stroke(width = tracoLargura)
                    )
                }
                "D10" -> {
                    val cx = w / 2f
                    val cy = h / 2f
                    val r = minOf(w, h) * 0.45f
                    val path = Path().apply {
                        moveTo(cx, cy - r)
                        lineTo(cx + r * 0.7f, cy)
                        lineTo(cx, cy + r)
                        lineTo(cx - r * 0.7f, cy)
                        close()
                    }
                    drawPath(path, color = corFundo)
                    drawPath(path, color = corContorno, style = Stroke(width = tracoLargura))
                }
                "D20" -> {
                    val cx = w / 2f
                    val cy = h / 2f
                    val r = minOf(w, h) * 0.48f
                    val path = Path().apply {
                        moveTo(cx, cy - r)
                        lineTo(cx + r * 0.866f, cy + r * 0.5f)
                        lineTo(cx - r * 0.866f, cy + r * 0.5f)
                        close()
                    }
                    drawPath(path, color = corFundo)
                    drawPath(path, color = corContorno, style = Stroke(width = tracoLargura))
                }
                "D100" -> {
                    val cx = w / 2f
                    val cy = h / 2f
                    val r = minOf(w, h) * 0.45f
                    val n = 10
                    val path = Path()
                    for (i in 0 until n) {
                        val angulo = -PI / 2 + 2 * PI * i / n
                        val x = cx + (r * cos(angulo)).toFloat()
                        val y = cy + (r * sin(angulo)).toFloat()
                        if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
                    }
                    path.close()
                    drawPath(path, color = corFundo)
                    drawPath(path, color = corContorno, style = Stroke(width = tracoLargura))
                }
            }
        }
        if (valor != null) {
            Text(
                text = valor.toString(),
                fontSize = 44.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF222222)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LancadorDeDadosApp() {
    var dadoSelecionado by remember { mutableStateOf("D6") }
    var valorAtual by remember { mutableStateOf<Int?>(null) }
    var resultado by remember { mutableStateOf("Clique no botão para lançar o dado") }

    val dados = listOf("D6", "D10", "D20", "D100")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Lançador de Dados",
            fontSize = 28.sp
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(text = "Escolha o tipo de dado:")

        dados.forEach { dado ->
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = dadoSelecionado == dado,
                    onClick = {
                        dadoSelecionado = dado
                        valorAtual = null
                        resultado = "Clique no botão para lançar o dado"
                    }
                )
                Text(text = dado)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                val valorSorteado = when (dadoSelecionado) {
                    "D6" -> Random.nextInt(1, 7)
                    "D10" -> Random.nextInt(1, 11)
                    "D20" -> Random.nextInt(1, 21)
                    "D100" -> Random.nextInt(1, 101)
                    else -> 0
                }

                valorAtual = valorSorteado
                resultado = "Resultado do $dadoSelecionado: $valorSorteado"
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = "Lançar dado")
        }

        Spacer(modifier = Modifier.height(24.dp))

        if (valorAtual != null) {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                DadoCanvas(tipo = dadoSelecionado, valor = valorAtual)
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        Text(
            text = resultado,
            fontSize = 20.sp
        )
    }
}
