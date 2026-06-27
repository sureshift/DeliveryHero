package com.deliveryninja.ui.common

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.deliveryninja.data.models.Customer
import com.deliveryninja.data.models.Platform
import com.deliveryninja.data.models.Restaurant

fun parseColor(hex: String): Color = try {
    Color(android.graphics.Color.parseColor(hex))
} catch (e: Exception) { Color(0xFFFC8019) }

// Reusable platform logo composable used across the whole app
@Composable
fun PlatformLogo(platform: Platform, size: Int = 40) {
    val color = parseColor(platform.colorHex)
    Box(
        modifier = Modifier
            .size(size.dp)
            .clip(RoundedCornerShape((size / 4).dp))
            .background(color.copy(alpha = 0.15f)),
        contentAlignment = Alignment.Center
    ) {
        if (platform.logoUri.isNotEmpty()) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(Uri.parse(platform.logoUri))
                    .crossfade(true)
                    .build(),
                contentDescription = platform.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize().clip(RoundedCornerShape((size / 4).dp))
            )
        } else {
            Text(
                platform.name.first().uppercase(),
                color = color,
                fontWeight = FontWeight.Bold,
                fontSize = (size / 2.5).sp
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddPlatformDialog(
    onDismiss: () -> Unit,
    onAdd: (name: String, colorHex: String, logoUri: String) -> Unit
) {
    val context = LocalContext.current
    val presetColors = listOf(
        "#FC8019", "#2563EB", "#FFD700", "#16A34A",
        "#9333EA", "#EF4444", "#EC4899", "#06B6D4",
        "#F97316", "#222222", "#6B7280", "#0EA5E9"
    )
    var name by remember { mutableStateOf("") }
    var selectedColor by remember { mutableStateOf(presetColors[0]) }
    var customColor by remember { mutableStateOf("") }
    var logoUri by remember { mutableStateOf("") }
    var showColorPicker by remember { mutableStateOf(false) }

    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            // Persist permission and save URI
            try {
                context.contentResolver.takePersistableUriPermission(
                    uri, android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (e: Exception) { /* not always available */ }
            logoUri = uri.toString()
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Platform", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Logo picker
                Text("Platform Logo", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color.Gray)
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    // Preview circle
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(parseColor(if (customColor.length == 7) customColor else selectedColor).copy(alpha = 0.15f))
                            .border(2.dp, parseColor(if (customColor.length == 7) customColor else selectedColor), RoundedCornerShape(12.dp))
                            .clickable { imagePicker.launch("image/*") },
                        contentAlignment = Alignment.Center
                    ) {
                        if (logoUri.isNotEmpty()) {
                            AsyncImage(
                                model = ImageRequest.Builder(context).data(Uri.parse(logoUri)).crossfade(true).build(),
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(12.dp))
                            )
                        } else {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.AddPhotoAlternate, null,
                                    tint = parseColor(if (customColor.length == 7) customColor else selectedColor),
                                    modifier = Modifier.size(24.dp))
                                Text("Logo", fontSize = 10.sp,
                                    color = parseColor(if (customColor.length == 7) customColor else selectedColor))
                            }
                        }
                    }
                    Column {
                        Button(
                            onClick = { imagePicker.launch("image/*") },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFC8019))
                        ) {
                            Icon(Icons.Default.Image, null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text(if (logoUri.isEmpty()) "Pick from Gallery" else "Change Logo", fontSize = 13.sp)
                        }
                        if (logoUri.isNotEmpty()) {
                            TextButton(onClick = { logoUri = "" }) {
                                Text("Remove", fontSize = 12.sp, color = Color.Gray)
                            }
                        }
                    }
                }

                // Platform Name
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Platform Name *") },
                    placeholder = { Text("e.g. Zomato, Dunzo...") },
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = { Icon(Icons.Default.Business, null) }
                )

                // Brand Color
                Text("Brand Color", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color.Gray)

                // Color presets grid
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    presetColors.chunked(6).forEach { row ->
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            row.forEach { hex ->
                                val isSelected = selectedColor == hex && customColor.isEmpty()
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(parseColor(hex))
                                        .border(
                                            width = if (isSelected) 3.dp else 0.dp,
                                            color = if (isSelected) Color.White else Color.Transparent,
                                            shape = CircleShape
                                        )
                                        .clickable { selectedColor = hex; customColor = "" },
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isSelected) {
                                        Icon(Icons.Default.Check, null, tint = Color.White,
                                            modifier = Modifier.size(18.dp))
                                    }
                                }
                            }
                        }
                    }
                }

                // Custom hex color input
                OutlinedTextField(
                    value = customColor,
                    onValueChange = { v ->
                        val clean = if (v.startsWith("#")) v else "#$v"
                        customColor = clean.take(7)
                    },
                    label = { Text("Custom Color (hex)") },
                    placeholder = { Text("#FF5733") },
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(
                                    if (customColor.length == 7) parseColor(customColor)
                                    else parseColor(selectedColor)
                                )
                        )
                    },
                    trailingIcon = {
                        if (customColor.isNotEmpty())
                            IconButton(onClick = { customColor = "" }) { Icon(Icons.Default.Clear, null) }
                    }
                )

                // Live preview
                val finalColor = if (customColor.length == 7) customColor else selectedColor
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = parseColor(finalColor))
                ) {
                    Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Box(modifier = Modifier.size(40.dp).clip(RoundedCornerShape(8.dp))
                            .background(Color.White.copy(alpha = 0.2f)), contentAlignment = Alignment.Center) {
                            if (logoUri.isNotEmpty()) {
                                AsyncImage(
                                    model = ImageRequest.Builder(context).data(Uri.parse(logoUri)).crossfade(true).build(),
                                    contentDescription = null, contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(8.dp))
                                )
                            } else {
                                Text(
                                    if (name.isNotEmpty()) name.first().uppercase() else "?",
                                    color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp
                                )
                            }
                        }
                        Column {
                            Text(if (name.isNotEmpty()) name else "Platform Name",
                                color = Color.White, fontWeight = FontWeight.Bold)
                            Text("Preview", color = Color.White.copy(alpha = 0.7f), fontSize = 11.sp)
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val finalColor = if (customColor.length == 7) customColor else selectedColor
                    onAdd(name.trim(), finalColor, logoUri)
                },
                enabled = name.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFC8019))
            ) { Text("Add Platform") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddDeliveryDialog(
    platforms: List<Platform>,
    restaurants: List<Restaurant>,
    customers: List<Customer>,
    onDismiss: () -> Unit,
    onAdd: (platformName: String, earnings: Double, tip: Double, distance: Double,
            restaurantName: String, pickupAddress: String, pickupLat: Double, pickupLon: Double,
            customerName: String, dropAddress: String, dropLat: Double, dropLon: Double, notes: String) -> Unit
) {
    var selectedPlatform by remember { mutableStateOf(platforms.firstOrNull()) }
    var earnings by remember { mutableStateOf("") }
    var tip by remember { mutableStateOf("") }
    var distance by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var platformExpanded by remember { mutableStateOf(false) }
    var restaurantQuery by remember { mutableStateOf("") }
    var selectedRestaurant by remember { mutableStateOf<Restaurant?>(null) }
    var pickupAddress by remember { mutableStateOf("") }
    var pickupLat by remember { mutableStateOf("") }
    var pickupLon by remember { mutableStateOf("") }
    var showRestaurantSuggestions by remember { mutableStateOf(false) }
    var customerQuery by remember { mutableStateOf("") }
    var selectedCustomer by remember { mutableStateOf<Customer?>(null) }
    var dropAddress by remember { mutableStateOf("") }
    var dropLat by remember { mutableStateOf("") }
    var dropLon by remember { mutableStateOf("") }
    var showCustomerSuggestions by remember { mutableStateOf(false) }

    val filteredRestaurants = restaurants.filter {
        restaurantQuery.length >= 2 && it.name.contains(restaurantQuery, ignoreCase = true)
    }
    val filteredCustomers = customers.filter {
        customerQuery.length >= 2 && (it.address.contains(customerQuery, ignoreCase = true) || it.area.contains(customerQuery, ignoreCase = true))
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Delivery", fontWeight = FontWeight.Bold) },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                // Platform picker
                ExposedDropdownMenuBox(expanded = platformExpanded, onExpandedChange = { platformExpanded = it }) {
                    OutlinedTextField(
                        value = selectedPlatform?.name ?: "Select Platform",
                        onValueChange = {}, readOnly = true,
                        label = { Text("Platform") },
                        leadingIcon = {
                            selectedPlatform?.let { p -> PlatformLogo(p, size = 28) }
                        },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(platformExpanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor()
                    )
                    ExposedDropdownMenu(expanded = platformExpanded, onDismissRequest = { platformExpanded = false }) {
                        platforms.forEach { p ->
                            DropdownMenuItem(
                                text = {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                        PlatformLogo(p, size = 32)
                                        Text(p.name)
                                    }
                                },
                                onClick = { selectedPlatform = p; platformExpanded = false }
                            )
                        }
                    }
                }

                OutlinedTextField(value = earnings, onValueChange = { earnings = it },
                    label = { Text("Earnings (₹) *") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = tip, onValueChange = { tip = it },
                    label = { Text("Tip (₹)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = distance, onValueChange = { distance = it },
                    label = { Text("Distance (km)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth())

                Divider()
                Text("📍 Pickup (Restaurant)", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                OutlinedTextField(value = restaurantQuery,
                    onValueChange = { restaurantQuery = it; selectedRestaurant = null; showRestaurantSuggestions = true },
                    label = { Text("Restaurant Name") }, modifier = Modifier.fillMaxWidth(),
                    trailingIcon = { if (restaurantQuery.isNotEmpty()) IconButton(onClick = { restaurantQuery = ""; selectedRestaurant = null }) { Icon(Icons.Default.Clear, null) } })
                if (showRestaurantSuggestions && filteredRestaurants.isNotEmpty()) {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        filteredRestaurants.take(4).forEach { r ->
                            Row(modifier = Modifier.fillMaxWidth().clickable {
                                selectedRestaurant = r; restaurantQuery = r.name; pickupAddress = r.address
                                pickupLat = if (r.lat != 0.0) r.lat.toString() else ""; pickupLon = if (r.lon != 0.0) r.lon.toString() else ""
                                showRestaurantSuggestions = false
                            }.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Store, null, modifier = Modifier.size(16.dp), tint = Color.Gray)
                                Spacer(Modifier.width(8.dp))
                                Column { Text(r.name, fontSize = 13.sp, fontWeight = FontWeight.Medium); if (r.address.isNotEmpty()) Text(r.address, fontSize = 11.sp, color = Color.Gray) }
                            }
                        }
                    }
                }
                OutlinedTextField(value = pickupAddress, onValueChange = { pickupAddress = it }, label = { Text("Pickup Address") }, modifier = Modifier.fillMaxWidth())
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = pickupLat, onValueChange = { pickupLat = it }, label = { Text("Lat") }, modifier = Modifier.weight(1f), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal))
                    OutlinedTextField(value = pickupLon, onValueChange = { pickupLon = it }, label = { Text("Lon") }, modifier = Modifier.weight(1f), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal))
                }

                Divider()
                Text("🏠 Drop (Customer)", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                OutlinedTextField(value = customerQuery,
                    onValueChange = { customerQuery = it; selectedCustomer = null; showCustomerSuggestions = true },
                    label = { Text("Customer / Area") }, modifier = Modifier.fillMaxWidth(),
                    trailingIcon = { if (customerQuery.isNotEmpty()) IconButton(onClick = { customerQuery = ""; selectedCustomer = null }) { Icon(Icons.Default.Clear, null) } })
                if (showCustomerSuggestions && filteredCustomers.isNotEmpty()) {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        filteredCustomers.take(4).forEach { c ->
                            Row(modifier = Modifier.fillMaxWidth().clickable {
                                selectedCustomer = c; customerQuery = c.area.ifEmpty { c.address }; dropAddress = c.address
                                dropLat = if (c.lat != 0.0) c.lat.toString() else ""; dropLon = if (c.lon != 0.0) c.lon.toString() else ""
                                showCustomerSuggestions = false
                            }.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.LocationOn, null, modifier = Modifier.size(16.dp), tint = Color.Gray)
                                Spacer(Modifier.width(8.dp))
                                Column { if (c.area.isNotEmpty()) Text(c.area, fontSize = 13.sp, fontWeight = FontWeight.Medium); Text(c.address, fontSize = 11.sp, color = Color.Gray) }
                            }
                        }
                    }
                }
                OutlinedTextField(value = dropAddress, onValueChange = { dropAddress = it }, label = { Text("Drop Address") }, modifier = Modifier.fillMaxWidth())
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = dropLat, onValueChange = { dropLat = it }, label = { Text("Lat") }, modifier = Modifier.weight(1f), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal))
                    OutlinedTextField(value = dropLon, onValueChange = { dropLon = it }, label = { Text("Lon") }, modifier = Modifier.weight(1f), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal))
                }
                Divider()
                OutlinedTextField(value = notes, onValueChange = { notes = it }, label = { Text("Notes") }, modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val e = earnings.toDoubleOrNull() ?: return@Button
                    onAdd(selectedPlatform?.name ?: "Other", e, tip.toDoubleOrNull() ?: 0.0, distance.toDoubleOrNull() ?: 0.0,
                        restaurantQuery, pickupAddress, pickupLat.toDoubleOrNull() ?: 0.0, pickupLon.toDoubleOrNull() ?: 0.0,
                        customerQuery, dropAddress, dropLat.toDoubleOrNull() ?: 0.0, dropLon.toDoubleOrNull() ?: 0.0, notes)
                },
                enabled = earnings.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFC8019))
            ) { Text("Add Delivery") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddExpenseDialog(onDismiss: () -> Unit, onAdd: (String, Double, String) -> Unit) {
    val types = listOf("FUEL", "MAINTENANCE", "FOOD", "OTHER")
    var type by remember { mutableStateOf("FUEL") }
    var amount by remember { mutableStateOf("") }
    var desc by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Expense") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
                    OutlinedTextField(value = type, onValueChange = {}, readOnly = true, label = { Text("Type") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) }, modifier = Modifier.fillMaxWidth().menuAnchor())
                    ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        types.forEach { t -> DropdownMenuItem(text = { Text(t) }, onClick = { type = t; expanded = false }) }
                    }
                }
                OutlinedTextField(value = amount, onValueChange = { amount = it }, label = { Text("Amount (₹)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = desc, onValueChange = { desc = it }, label = { Text("Description") }, modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = { Button(onClick = { amount.toDoubleOrNull()?.let { onAdd(type, it, desc) } }, enabled = amount.isNotBlank()) { Text("Add") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddRestaurantDialog(onDismiss: () -> Unit, onAdd: (String, String, Double, Double, String) -> Unit) {
    var name by remember { mutableStateOf("") }; var address by remember { mutableStateOf("") }
    var lat by remember { mutableStateOf("") }; var lon by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    AlertDialog(onDismissRequest = onDismiss, title = { Text("Add Restaurant") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Restaurant Name *") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = address, onValueChange = { address = it }, label = { Text("Address") }, modifier = Modifier.fillMaxWidth())
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = lat, onValueChange = { lat = it }, label = { Text("Latitude") }, modifier = Modifier.weight(1f), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal))
                    OutlinedTextField(value = lon, onValueChange = { lon = it }, label = { Text("Longitude") }, modifier = Modifier.weight(1f), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal))
                }
                OutlinedTextField(value = notes, onValueChange = { notes = it }, label = { Text("Notes") }, modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = { Button(onClick = { if (name.isNotBlank()) onAdd(name, address, lat.toDoubleOrNull() ?: 0.0, lon.toDoubleOrNull() ?: 0.0, notes) }, enabled = name.isNotBlank()) { Text("Add") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddCustomerDialog(onDismiss: () -> Unit, onAdd: (String, String, String, Double, Double, String) -> Unit) {
    var name by remember { mutableStateOf("") }; var address by remember { mutableStateOf("") }
    var area by remember { mutableStateOf("") }; var lat by remember { mutableStateOf("") }
    var lon by remember { mutableStateOf("") }; var notes by remember { mutableStateOf("") }
    AlertDialog(onDismissRequest = onDismiss, title = { Text("Add Customer Location") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Customer Name") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = area, onValueChange = { area = it }, label = { Text("Area / Locality *") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = address, onValueChange = { address = it }, label = { Text("Full Address") }, modifier = Modifier.fillMaxWidth())
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = lat, onValueChange = { lat = it }, label = { Text("Latitude") }, modifier = Modifier.weight(1f), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal))
                    OutlinedTextField(value = lon, onValueChange = { lon = it }, label = { Text("Longitude") }, modifier = Modifier.weight(1f), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal))
                }
                OutlinedTextField(value = notes, onValueChange = { notes = it }, label = { Text("Notes") }, modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = { Button(onClick = { if (area.isNotBlank()) onAdd(name, address, area, lat.toDoubleOrNull() ?: 0.0, lon.toDoubleOrNull() ?: 0.0, notes) }, enabled = area.isNotBlank()) { Text("Add") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
