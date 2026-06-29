const fileId = window.location.pathname.split('/').pop()

init()

async function init() {
    let token = sessionStorage.getItem('accessToken')

    if (!token) {
        token = await refreshAccessToken()

        if (!token) {
            window.location.href = '/'
            return
        }
    }

    if (!fileId || fileId === 'file.html') {
        document.getElementById('fileName').textContent = 'Файл не найден'
        return
    }

    const file = await getFileById(fileId)

    if (!file) {
        document.getElementById('fileName').textContent = 'Файл не найден'
        return
    }

    renderFileInfo(file)

    document.getElementById('downloadBtn').addEventListener('click', () => {
        downloadFile(fileId, file.filename)
    })

    initLogoLink()
}

function initLogoLink() {
    document.getElementById('logo').addEventListener('click', () => {
        window.location.href = '/'
    })
}

async function getFileById(fileId) {
    let token = sessionStorage.getItem('accessToken')

    let response = await fetch(`http://localhost:8080/files/file-by-id?fileId=${fileId}`, {
        method: "GET",
        headers: {
            "Authorization": `Bearer ${token}`,
            "Content-Type": "application/json"
        }
    })

    if (response.status === 401) {
        token = await refreshAccessToken()
        if (!token) return null

        response = await fetch(`http://localhost:8080/files/file-by-id?fileId=${fileId}`, {
            method: "GET",
            headers: {
                "Authorization": `Bearer ${token}`,
                "Content-Type": "application/json"
            }
        })
    }

    if (!response.ok) return null

    return await response.json()
}

function renderFileInfo(file) {
    document.title = file.filename

    document.getElementById('fileName').textContent = file.filename
    document.getElementById('fileMeta').textContent = formatFileSize(file.size) + ' · ' + file.contentType
    document.getElementById('fileDate').textContent = 'Загружен: ' + new Date(file.createdAt).toLocaleDateString('ru-RU')
}

async function downloadFile(fileId, filename) {
    let token = sessionStorage.getItem('accessToken')

    let response = await fetch("http://localhost:8080/files/generate-download-url", {
        method: "POST",
        headers: {
            "Authorization": `Bearer ${token}`,
            "Content-Type": "application/json"
        },
        body: JSON.stringify({ fileId })
    })

    if (response.status === 401) {
        token = await refreshAccessToken()
        if (!token) return

        response = await fetch("http://localhost:8080/files/generate-download-url", {
            method: "POST",
            headers: {
                "Authorization": `Bearer ${token}`,
                "Content-Type": "application/json"
            },
            body: JSON.stringify({ fileId })
        })
    }

    if (!response.ok) return

    const { url } = await response.json()

    const fileResponse = await fetch(url)
    const blob = await fileResponse.blob()
    const blobUrl = URL.createObjectURL(blob)

    const a = document.createElement('a')
    a.href = blobUrl
    a.download = filename
    document.body.appendChild(a)
    a.click()
    document.body.removeChild(a)
    URL.revokeObjectURL(blobUrl)
}

function formatFileSize(bytes) {
    if (bytes < 1024) return bytes + ' B'
    if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(1) + ' KB'
    return (bytes / (1024 * 1024)).toFixed(1) + ' MB'
}

async function refreshAccessToken() {
    const refreshToken = sessionStorage.getItem("refreshToken")

    const response = await fetch(
        "http://localhost:8180/realms/filesharing/protocol/openid-connect/token",
        {
            method: "POST",
            headers: { "Content-Type": "application/x-www-form-urlencoded" },
            body: new URLSearchParams({
                grant_type: "refresh_token",
                client_id: "frontend-client",
                refresh_token: refreshToken
            })
        }
    )

    if (!response.ok) {
        sessionStorage.clear()
        return null
    }

    const data = await response.json()
    sessionStorage.setItem("accessToken", data.access_token)
    sessionStorage.setItem("refreshToken", data.refresh_token)
    return data.access_token
}