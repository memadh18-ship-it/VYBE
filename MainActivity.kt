package com.vybe.app

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer

private val Bg = Color(0xFF08090D)
private val Card = Color(0xFF14161D)
private val Accent = Color(0xFFB86BFF)

class MainActivity : ComponentActivity() {
    private lateinit var player: ExoPlayer
    private val requestAudio = registerForActivityResult(ActivityResultContracts.RequestPermission()) { }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        player = ExoPlayer.Builder(this).build()
        if (android.os.Build.VERSION.SDK_INT >= 33 && ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_AUDIO) != PackageManager.PERMISSION_GRANTED) requestAudio.launch(Manifest.permission.READ_MEDIA_AUDIO)
        setContent { VybeApp(player) }
    }
    override fun onDestroy() { player.release(); super.onDestroy() }
}

data class Song(val title: String, val artist: String, val url: String? = null)

@Composable
fun VybeApp(player: ExoPlayer) {
    var tab by remember { mutableStateOf(0) }
    var query by remember { mutableStateOf("") }
    var current by remember { mutableStateOf<Song?>(null) }
    val demo = remember { listOf(
        Song("VYBE Radio — Demo", "VYBE", "https://streaming.jamendo.com/JamendoLounge"),
        Song("Discover Indie", "VYBE Picks"), Song("Night Drive", "VYBE Picks"), Song("Afterglow", "VYBE Picks")
    ) }
    val filtered = demo.filter { it.title.contains(query, true) || it.artist.contains(query, true) }
    MaterialTheme(colorScheme = darkColorScheme(background = Bg, surface = Card, primary = Accent)) {
        Scaffold(containerColor = Bg, bottomBar = {
            NavigationBar(containerColor = Color(0xFF0E1015)) {
                listOf(Icons.Default.Home to "Home", Icons.Default.Search to "Search", Icons.Default.Favorite to "Liked", Icons.Default.LibraryMusic to "Library").forEachIndexed { i, p ->
                    NavigationBarItem(selected = tab == i, onClick = { tab = i }, icon = { Icon(p.first, null) }, label = { Text(p.second) })
                }
            }
        }) { pad ->
            Column(Modifier.fillMaxSize().padding(pad).padding(horizontal = 18.dp)) {
                Spacer(Modifier.height(18.dp)); Text("VYBE", fontSize = 32.sp, fontWeight = FontWeight.Black, color = Color.White)
                Text("music without the clutter.", color = Color.Gray)
                Spacer(Modifier.height(18.dp))
                OutlinedTextField(value = query, onValueChange = { query = it }, modifier = Modifier.fillMaxWidth(), placeholder = { Text("Search songs, artists, albums") }, leadingIcon = { Icon(Icons.Default.Search, null) }, singleLine = true, shape = RoundedCornerShape(18.dp))
                Spacer(Modifier.height(20.dp))
                if (tab == 0) HomeContent(filtered, current, player) { current = it }
                else LibraryContent(tab)
                current?.let { song ->
                    Spacer(Modifier.height(12.dp)); MiniPlayer(song, player)
                }
            }
        }
    }
}

@Composable fun HomeContent(songs: List<Song>, current: Song?, player: ExoPlayer, onPlay: (Song)->Unit) {
    Text("Made for you", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color.White)
    Spacer(Modifier.height(12.dp))
    Row(Modifier.fillMaxWidth().height(125.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        Feature("🔥", "Trending", "Worldwide")
        Feature("🌙", "Night Vibes", "Chill")
        Feature("🎧", "Indie", "Fresh")
    }
    Spacer(Modifier.height(22.dp)); Text("Quick picks", fontSize = 20.sp, fontWeight = FontWeight.Bold)
    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.weight(1f)) { items(songs) { song -> SongRow(song) { onPlay(song); song.url?.let { player.setMediaItem(MediaItem.fromUri(it)); player.prepare(); player.play() } } } }
}
@Composable fun Feature(icon: String, title: String, sub: String) { Card(Modifier.width(110.dp).fillMaxHeight(), shape = RoundedCornerShape(20.dp)) { Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.SpaceBetween) { Text(icon, fontSize = 28.sp); Text(title, fontWeight = FontWeight.Bold); Text(sub, color = Color.Gray, fontSize = 12.sp) } } }
@Composable fun SongRow(song: Song, onClick: ()->Unit) { Row(Modifier.fillMaxWidth().clickable { onClick() }.background(Card, RoundedCornerShape(16.dp)).padding(12.dp), verticalAlignment = Alignment.CenterVertically) { Box(Modifier.size(52.dp).background(Brush.linearGradient(listOf(Accent, Color(0xFF252B5A))), RoundedCornerShape(12.dp))); Spacer(Modifier.width(12.dp)); Column(Modifier.weight(1f)) { Text(song.title, fontWeight = FontWeight.SemiBold); Text(song.artist, color = Color.Gray, fontSize = 13.sp) }; Icon(Icons.Default.PlayArrow, null) } }
@Composable fun MiniPlayer(song: Song, player: ExoPlayer) { Row(Modifier.fillMaxWidth().background(Color(0xFF1B1D26), RoundedCornerShape(18.dp)).padding(10.dp), verticalAlignment = Alignment.CenterVertically) { Column(Modifier.weight(1f)) { Text(song.title, fontWeight = FontWeight.Bold); Text(song.artist, color = Color.Gray, fontSize = 12.sp) }; IconButton({ if (player.isPlaying) player.pause() else player.play() }) { Icon(if (player.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow, null) } } }
@Composable fun LibraryContent(tab: Int) { val title = when(tab) {1->"Search";2->"Liked songs";else->"Your Library"}; Text(title, fontSize=24.sp,fontWeight=FontWeight.Bold); Spacer(Modifier.height(20.dp)); Text("Your VYBE collection will appear here.", color=Color.Gray) }
