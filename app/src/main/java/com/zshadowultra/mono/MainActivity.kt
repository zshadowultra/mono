package com.zshadowultra.mono

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.composables.icons.lucide.Ellipsis
import com.composables.icons.lucide.Lucide
import com.zshadowultra.mono.ui.theme.BgDark
import com.zshadowultra.mono.ui.theme.BgLight
import com.zshadowultra.mono.ui.theme.CardDark
import com.zshadowultra.mono.ui.theme.CardLight
import com.zshadowultra.mono.ui.theme.MonoTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MonoTheme {
                EditorScreen()
            }
        }
    }
}

@Composable
fun EditorScreen(modifier: Modifier = Modifier) {
    val dark = MaterialTheme.colorScheme.background == BgDark
    val bg = if (dark) BgDark else BgLight
    val card = if (dark) CardDark else CardLight
    val fg = if (dark) Color.White else Color.Black

    var text by rememberSaveable { mutableStateOf("") }

    Surface(color = bg, modifier = modifier.fillMaxSize()) {
        Column(
            Modifier
                .fillMaxSize()
                .systemBarsPadding()
                .padding(horizontal = 16.dp),
        ) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
            ) {
                Text(
                    text = "Mononote",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = fg,
                    modifier = Modifier.align(Alignment.Center),
                )
                Surface(
                    onClick = {},
                    shape = CircleShape,
                    color = if (dark) CardDark else Color.White,
                    shadowElevation = 2.dp,
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .size(44.dp),
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Lucide.Ellipsis,
                            contentDescription = null,
                            tint = fg,
                        )
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            BasicTextField(
                value = text,
                onValueChange = { text = it },
                textStyle = TextStyle(fontSize = 17.sp, color = fg),
                cursorBrush = SolidColor(fg),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(card, RoundedCornerShape(24.dp))
                    .padding(16.dp),
                decorationBox = { inner ->
                    Box {
                        if (text.isEmpty()) {
                            Text("Start typing...", fontSize = 17.sp, color = fg.copy(alpha = 0.3f))
                        }
                        inner()
                    }
                },
            )

            Spacer(Modifier.height(12.dp))

            Button(
                onClick = {},
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(25.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (dark) Color.White else Color.Black,
                    contentColor = if (dark) Color.Black else Color.White,
                ),
            ) {
                Text("Done", fontSize = 17.sp, fontWeight = FontWeight.SemiBold)
            }

            Spacer(Modifier.height(12.dp))
        }
    }
}
