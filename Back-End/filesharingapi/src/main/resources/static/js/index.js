init();

async function init() {
    authModalInit()
    initUploadBtn()
    initDeleteBtn()

    if (isAurorize()) {
        const filesData = await getFilesData()
        renderFiles(filesData)
    } else {
        document.getElementById('authModal').classList.add('active')
    }
}

function authModalInit() {
    const loginTab = document.getElementById('loginTab')
    const registerTab = document.getElementById('registerTab')

    document.getElementById('toRegister').addEventListener('click', () => {
        loginTab.style.display = 'none'
        registerTab.style.display = 'flex'
    })

    document.getElementById('toLogin').addEventListener('click', () => {
        registerTab.style.display = 'none'
        loginTab.style.display = 'flex'
    })

    document.getElementById('regButton').addEventListener('click', () => {
        registrationAccount()
    })

    document.getElementById('authButton').addEventListener('click', () => {
        authorizationAccount()
    })
}

async function registrationAccount() {
    const login = document.getElementById('loginInputRegistation').value.trim()
    const firstName = document.getElementById('nameInputRegistation').value.trim()
    const lastName = document.getElementById('surnameInputRegistation').value.trim()
    const email = document.getElementById('emailInputRegistation').value.trim()
    const password = document.getElementById('passwordInputRegistation').value

    if (!login || !firstName || !lastName || !email || !password) {
        alert('Заполните все поля')
        return
    }

    try {
        const response = await fetch('http://localhost:8080/api/public/register', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({
                username: login,
                firstName: firstName,
                lastName: lastName,
                email: email,
                password: password
            })
        })

        const text = await response.text()

        if (!response.ok) {
            console.error('Ошибка регистрации:', response.status, text)
            alert('Ошибка регистрации: ' + text)
            return
        }

        alert('Регистрация успешна. Теперь войдите в аккаунт.')

        document.getElementById('registerTab').style.display = 'none'
        document.getElementById('loginTab').style.display = 'flex'

        document.getElementById('loginInputAutorization').value = login
        document.getElementById('passwordInputAutorization').value = password

    } catch (error) {
        console.error('Ошибка запроса регистрации:', error)
        alert('Не удалось выполнить регистрацию')
    }
}

async function authorizationAccount() {
    const login = document.getElementById('loginInputAutorization').value
    const password = document.getElementById('passwordInputAutorization').value

    const response = await fetch('http://localhost:8180/realms/filesharing/protocol/openid-connect/token', {
        method: 'POST',
        headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
        body: new URLSearchParams({
            grant_type: 'password',
            client_id: 'frontend-client',
            username: login,
            password: password
        })
    })

    const data = await response.json()

    if (!data.access_token) {
        alert('Ошибка авторизации')
        return
    }

    sessionStorage.setItem('accessToken', data.access_token)
    sessionStorage.setItem('refreshToken', data.refresh_token)
    document.getElementById('authModal').classList.remove('active')

    const filesData = await getFilesData()
    renderFiles(filesData)
}

async function refreshAccessToken() {
    const refreshToken = sessionStorage.getItem("refreshToken")

    if (!refreshToken) {
        sessionStorage.clear()
        return null
    }

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
        const text = await response.text()
        console.error("Ошибка refresh token:", response.status, text)
        sessionStorage.clear()
        return null
    }

    const data = await response.json()
    sessionStorage.setItem("accessToken", data.access_token)
    sessionStorage.setItem("refreshToken", data.refresh_token)
    return data.access_token
}

async function getFilesData() {
    let token = sessionStorage.getItem('accessToken')

    let response = await fetch("http://localhost:8080/files/user-files", {
        method: "GET",
        headers: {
            "Authorization": `Bearer ${token}`,
            "Content-Type": "application/json"
        }
    })

    if (response.status === 401) {
        token = await refreshAccessToken()
        if (!token) return []

        response = await fetch("http://localhost:8080/files/user-files", {
            method: "GET",
            headers: {
                "Authorization": `Bearer ${token}`,
                "Content-Type": "application/json"
            }
        })
    }

    return await response.json()
}

function isAurorize() {
    return !!sessionStorage.getItem('accessToken')
}

function initUploadBtn() {
    const fileInput = document.getElementById('fileInput')

    document.getElementById('upload-btn').addEventListener('click', () => {
        fileInput.click()
    })

    fileInput.addEventListener('change', async () => {
        const file = fileInput.files[0]
        if (!file) return

        let token = sessionStorage.getItem('accessToken')

        let response = await fetch('http://localhost:8080/files/generate-upload-url', {
            method: "POST",
            headers: {
                Authorization: `Bearer ${token}`,
                "Content-Type": "application/json"
            },
            body: JSON.stringify({ fileName: file.name, fileType: file.type })
        })

        if (response.status === 401) {
            token = await refreshAccessToken()
            if (!token) return

            response = await fetch('http://localhost:8080/files/generate-upload-url', {
                method: "POST",
                headers: {
                    Authorization: `Bearer ${token}`,
                    "Content-Type": "application/json"
                },
                body: JSON.stringify({ fileName: file.name, fileType: file.type })
            })
        }

        if (!response.ok) return

        const { presignedUrl, key } = await response.json()

        const uploadResponse = await fetch(presignedUrl, {
            method: "PUT",
            body: file,
            headers: { "Content-Type": file.type }
        })

        if (!uploadResponse.ok) return

        const confirmResponse = await fetch("http://localhost:8080/files/confirm-upload-file", {
            method: "POST",
            headers: {
                "Content-Type": "application/json",
                "Authorization": `Bearer ${token}`
            },
            body: JSON.stringify({
                fileName: file.name,
                objectKey: key,
                contentType: file.type,
                size: file.size,
                isPublic: false,
            })
        })

        if (!confirmResponse.ok) return

        fileInput.value = ''
        const filesData = await getFilesData()
        renderFiles(filesData)
    })
}

function renderFiles(files) {
    const filePanel = document.querySelector('.file-panel')
    filePanel.innerHTML = ''
    files.forEach(file => {
        filePanel.appendChild(createFileCard(file))
    })
}

function getFileIcon(contentType) {
    if (!contentType) return 'ti-file'
    if (contentType.startsWith('image/')) return 'ti-photo'
    if (contentType.startsWith('video/')) return 'ti-video'
    if (contentType.startsWith('audio/')) return 'ti-music'
    if (contentType === 'application/pdf') return 'ti-file-type-pdf'
    if (contentType.includes('word')) return 'ti-file-type-doc'
    if (contentType.includes('excel') || contentType.includes('spreadsheet')) return 'ti-file-type-xls'
    if (contentType.includes('zip') || contentType.includes('rar')) return 'ti-file-zip'
    if (contentType.startsWith('text/')) return 'ti-file-type-txt'
    return 'ti-file'
}

function createFileCard(file) {
    const card = document.createElement('div')
    card.className = 'file-card'
    card.dataset.fileId = file.id

    card.innerHTML = `
        <div class="file-icon">
            <i class="ti ${getFileIcon(file.contentType)}" aria-hidden="true"></i>
        </div>
        <div class="file-card-info">
            <p class="file-card-name">${file.filename}</p>
            <p class="file-card-meta">${formatFileSize(file.size)}</p>
        </div>
        <span class="visibility-label">${file.isPublic ? 'Публичный' : 'Приватный'}</span>
        <label class="file-card-toggle">
            <input type="checkbox" class="public-toggle" ${file.isPublic ? 'checked' : ''}>
            <span class="toggle-slider"></span>
        </label>
        <button class="share-btn" title="Поделиться" aria-label="Поделиться">
            <i class="ti ti-share-2" aria-hidden="true"></i>
        </button>
        <button class="download-btn" title="Скачать" aria-label="Скачать">
            <i class="ti ti-cloud-download" aria-hidden="true"></i>
        </button>
    `

    card.addEventListener('click', (e) => {
        if (e.target.closest('.file-card-toggle') || e.target.closest('.share-btn') || e.target.closest('.download-btn')) return
        card.classList.toggle('selected')
    })

    card.querySelector('.download-btn').addEventListener('click', async (e) => {
        e.stopPropagation()
        await downloadFile(file.id, file.filename)
    })

    card.querySelector('.public-toggle').addEventListener('change', (e) => {
        const label = card.querySelector('.visibility-label')
        label.textContent = e.target.checked ? 'Публичный' : 'Приватный'
        togglePublicState(file.id, e.target.checked)
    })

    card.querySelector('.share-btn').addEventListener('click', (e) => {
        e.stopPropagation()
        copyFileId(file.id)
    })

    return card
}

function formatFileSize(bytes) {
    if (bytes < 1024) return bytes + ' B'
    if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(1) + ' KB'
    return (bytes / (1024 * 1024)).toFixed(1) + ' MB'
}

async function togglePublicState(fileId, isPublic) {
    let token = sessionStorage.getItem('accessToken')

    let response = await fetch("http://localhost:8080/files/switch-file-state", {
        method: "POST",
        headers: {
            "Authorization": `Bearer ${token}`,
            "Content-Type": "application/json"
        },
        body: JSON.stringify({ id: fileId, isPublic })
    })

    if (response.status === 401) {
        token = await refreshAccessToken()
        if (!token) return

        response = await fetch("http://localhost:8080/files/switch-file-state", {
            method: "POST",
            headers: {
                "Authorization": `Bearer ${token}`,
                "Content-Type": "application/json"
            },
            body: JSON.stringify({ id: fileId, isPublic })
        })
    }

    const data = await response.json()
    if (!data.isSwitched) alert(data.message)
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

function getSelectedFiles() {
    const selectedCards = document.querySelectorAll('.file-card.selected')
    return Array.from(selectedCards).map(card => card.dataset.fileId)
}

async function copyFileId(fileId) {
    try {
        await navigator.clipboard.writeText("http://localhost:8080/file/" + fileId)
    } catch (err) {
        console.error('Не удалось скопировать', err)
    }
}

async function deleteFiles(fileIds) {
    let token = sessionStorage.getItem('accessToken')

    let response = await fetch("http://localhost:8080/files/delete-files", {
        method: "DELETE",
        headers: {
            "Authorization": `Bearer ${token}`,
            "Content-Type": "application/json"
        },
        body: JSON.stringify({ fileIds })
    })

    if (response.status === 401) {
        token = await refreshAccessToken()
        if (!token) return

        response = await fetch("http://localhost:8080/files/delete-files", {
            method: "DELETE",
            headers: {
                "Authorization": `Bearer ${token}`,
                "Content-Type": "application/json"
            },
            body: JSON.stringify({ fileIds })
        })
    }

    return await response.json()
}

function initDeleteBtn() {
    document.getElementById('delete-btn').addEventListener('click', async () => {
        const fileIds = getSelectedFiles()

        if (fileIds.length === 0) return

        await deleteFiles(fileIds)
        const filesData = await getFilesData()
        renderFiles(filesData)
    })
}