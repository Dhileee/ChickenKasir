package com.fadhil.chickenkasir.firebase

import android.net.Uri
import com.fadhil.chickenkasir.model.Kategori
import com.fadhil.chickenkasir.model.Menu
import com.fadhil.chickenkasir.model.Transaksi
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeout
import kotlinx.coroutines.TimeoutCancellationException
import com.fadhil.chickenkasir.model.User

object FirebaseHelper {

    private val db = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()

    // MENU OPERATIONS
    suspend fun getAllMenu(): List<Menu> {
        return try {
            val snapshot = db.collection("menu").get().await()
            snapshot.documents.mapNotNull { doc ->
                doc.toObject(Menu::class.java)?.apply { id = doc.id }
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getMenuByKategori(kategori: String): List<Menu> {
        return try {
            val snapshot = db.collection("menu")
                .whereEqualTo("kategori", kategori)
                .get()
                .await()
            snapshot.documents.mapNotNull { doc ->
                doc.toObject(Menu::class.java)?.apply { id = doc.id }
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun addMenu(menu: Menu): String {
        return try {
            val docRef = db.collection("menu").add(menu).await()
            docRef.id
        } catch (e: Exception) {
            ""
        }
    }

    suspend fun updateMenu(menuId: String, menu: Menu) {
        try {
            db.collection("menu").document(menuId).set(menu).await()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun deleteMenu(menuId: String) {
        try {
            db.collection("menu").document(menuId).delete().await()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // KATEGORI OPERATIONS
    suspend fun getAllKategori(): List<Kategori> {
        return try {
            val snapshot = db.collection("kategori").get().await()
            snapshot.documents.mapNotNull { doc ->
                doc.toObject(Kategori::class.java)?.apply { id = doc.id }
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun addKategori(kategori: Kategori): String {
        return try {
            val docRef = db.collection("kategori").add(kategori).await()
            docRef.id
        } catch (e: Exception) {
            ""
        }
    }

    suspend fun deleteKategori(kategoriId: String) {
        try {
            db.collection("kategori").document(kategoriId).delete().await()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // TRANSAKSI OPERATIONS
    suspend fun addTransaksi(transaksi: Transaksi): String {
        return try {
            android.util.Log.d("FIREBASE_DEBUG", "addTransaksi START")
            android.util.Log.d("FIREBASE_DEBUG", "transaksi data: total=${transaksi.total}, metode=${transaksi.metodePembayaran}")

            android.util.Log.d("FIREBASE_DEBUG", "Calling db.collection(transaksi).add()...")

            val docRef = withTimeout(10000) {
                db.collection("transaksi").add(transaksi).await()
            }

            android.util.Log.d("FIREBASE_DEBUG", "Document added successfully with ID: ${docRef.id}")

            docRef.id

        } catch (e: TimeoutCancellationException) {
            android.util.Log.e("FIREBASE_DEBUG", "TIMEOUT: Firebase add took too long")
            e.printStackTrace()
            "TIMEOUT_${System.currentTimeMillis()}"

        } catch (e: Exception) {
            android.util.Log.e("FIREBASE_DEBUG", "ERROR in addTransaksi: ${e.javaClass.simpleName} - ${e.message}")
            e.printStackTrace()
            "ERROR_${System.currentTimeMillis()}"
        }
    }

    suspend fun getAllTransaksi(): List<Transaksi> {
        return try {
            val snapshot = db.collection("transaksi")
                .orderBy("tanggal", Query.Direction.DESCENDING)
                .get()
                .await()
            snapshot.documents.mapNotNull { doc ->
                doc.toObject(Transaksi::class.java)?.apply { id = doc.id }
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    // USER OPERATIONS
    suspend fun createUser(user: User): String {
        return try {
            android.util.Log.d("FIREBASE_DEBUG", "createUser START - email: ${user.email}")

            val docRef = db.collection("users").document(user.id)
            docRef.set(user).await()

            android.util.Log.d("FIREBASE_DEBUG", "User created with ID: ${user.id}")

            user.id
        } catch (e: Exception) {
            android.util.Log.e("FIREBASE_DEBUG", "ERROR creating user: ${e.message}")
            e.printStackTrace()
            ""
        }
    }

    suspend fun getUser(userId: String): User? {
        return try {
            val doc = db.collection("users").document(userId).get().await()
            doc.toObject(User::class.java)?.apply { id = doc.id }
        } catch (e: Exception) {
            null
        }
    }

    suspend fun getUserByEmail(email: String): User? {
        return try {
            val snapshot = db.collection("users")
                .whereEqualTo("email", email)
                .limit(1)
                .get()
                .await()

            if (snapshot.documents.isNotEmpty()) {
                snapshot.documents[0].toObject(User::class.java)?.apply {
                    id = snapshot.documents[0].id
                }
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    suspend fun getUserByUsername(username: String): User? {
        return try {
            val snapshot = db.collection("users")
                .whereEqualTo("username", username)
                .limit(1)
                .get()
                .await()

            if (snapshot.documents.isNotEmpty()) {
                snapshot.documents[0].toObject(User::class.java)?.apply {
                    id = snapshot.documents[0].id
                }
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    // Fungsi updateUser yang sudah dipindahkan ke posisi yang benar
    suspend fun updateUser(userId: String, user: User) {
        try {
            android.util.Log.d("FIREBASE_DEBUG", "updateUser START - userId: $userId")

            db.collection("users").document(userId).set(user).await()

            android.util.Log.d("FIREBASE_DEBUG", "User updated successfully")
        } catch (e: Exception) {
            android.util.Log.e("FIREBASE_DEBUG", "ERROR updating user: ${e.message}")
            e.printStackTrace()
        }
    }

    // IMAGE UPLOAD
    suspend fun uploadMenuImage(imageUri: Uri, menuId: String): String {
        return try {
            val storageRef = storage.reference.child("menu_images/$menuId.jpg")
            storageRef.putFile(imageUri).await()
            val downloadUrl = storageRef.downloadUrl.await()
            downloadUrl.toString()
        } catch (e: Exception) {
            e.printStackTrace()
            ""
        }
    }

    // INIT DEFAULT DATA
    suspend fun initDefaultData() {
        val menuCount = db.collection("menu").get().await().size()
        val kategoriCount = db.collection("kategori").get().await().size()

        if (kategoriCount == 0) {
            addKategori(Kategori(nama = "Reguler"))
            addKategori(Kategori(nama = "Paket"))
            addKategori(Kategori(nama = "Minuman"))
            addKategori(Kategori(nama = "Spesial"))
        }

        if (menuCount == 0) {
            addMenu(Menu(nama = "Ayam Original", harga = 15000, gambar = "ayam_original", kategori = "Reguler", deskripsi = "Ayam goreng original renyah"))
            addMenu(Menu(nama = "Ayam Geprek", harga = 17000, gambar = "ayam_geprek", kategori = "Reguler", deskripsi = "Ayam geprek pedas sambal"))
            addMenu(Menu(nama = "Paket Hemat A", harga = 25000, gambar = "paket_a", kategori = "Paket", deskripsi = "1 ayam + nasi + es teh"))
            addMenu(Menu(nama = "Es Teh", harga = 3000, gambar = "es_teh", kategori = "Minuman", deskripsi = "Es teh manis segar"))
        }
    }
}