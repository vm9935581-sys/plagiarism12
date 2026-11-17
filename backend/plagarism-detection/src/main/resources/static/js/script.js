// ==========================================
// Plagiarism Detection System - script.js
// ==========================================

// Demo backend URL
const API_URL = "http://localhost:8080/api";


// ==========================================
// LOGIN
// ==========================================

document.addEventListener("DOMContentLoaded", function () {

    const loginForm = document.getElementById("loginForm");

    if (loginForm) {

        loginForm.addEventListener("submit", function (event) {

            event.preventDefault();

            const username =
                document.getElementById("username").value.trim();

            const password =
                document.getElementById("password").value.trim();

            const message =
                document.getElementById("loginMessage");


            // Demo login
            if (username === "admin" && password === "admin123") {

                message.className =
                    "alert alert-success mt-3";

                message.textContent =
                    "Login successful. Redirecting...";

                localStorage.setItem(
                    "loggedIn",
                    "true"
                );

                localStorage.setItem(
                    "username",
                    username
                );

                setTimeout(function () {

                    window.location.href =
                        "dashboard.html";

                }, 800);

            } else {

                message.className =
                    "alert alert-danger mt-3";

                message.textContent =
                    "Invalid username or password.";

            }

        });

    }


    // Load dashboard when dashboard page opens
    if (document.getElementById("recentReports")) {

        loadDashboard();

    }


    // Load result when result page opens
    if (document.getElementById("similarityPercentage")) {

        loadResult();

    }

});


// ==========================================
// CHECK LOGIN
// ==========================================

function checkLogin() {

    const loggedIn =
        localStorage.getItem("loggedIn");

    if (loggedIn !== "true") {

        window.location.href =
            "login.html";

    }

}


// ==========================================
// LOGOUT
// ==========================================

function logout() {

    localStorage.removeItem("loggedIn");
    localStorage.removeItem("username");

    window.location.href =
        "login.html";

}


// ==========================================
// CHECK PLAGIARISM
// ==========================================

function checkPlagiarism() {

    const textElement =
        document.getElementById("plagiarismText");

    const fileElement =
        document.getElementById("documentFile");

    const nameElement =
        document.getElementById("documentName");

    const message =
        document.getElementById("uploadMessage");


    const text =
        textElement
            ? textElement.value.trim()
            : "";

    const file =
        fileElement
            ? fileElement.files[0]
            : null;

    let documentName =
        nameElement
            ? nameElement.value.trim()
            : "";


    // Check whether text or file exists
    if (!text && !file) {

        showMessage(
            message,
            "Please paste text or upload a document.",
            "danger"
        );

        return;

    }


    if (!documentName) {

        if (file) {

            documentName = file.name;

        } else {

            documentName = "Untitled Document";

        }

    }


    // Show progress
    const progress =
        document.getElementById("checkingProgress");

    const progressBar =
        document.getElementById("progressBar");

    if (progress) {

        progress.classList.remove("d-none");

    }


    let percentage = 0;


    const interval =
        setInterval(function () {

            percentage += 10;

            if (progressBar) {

                progressBar.style.width =
                    percentage + "%";

            }


            if (percentage >= 100) {

                clearInterval(interval);

                // Calculate demo similarity
                const similarity =
                    calculateSimilarity(text);

                const result =
                    createResult(
                        documentName,
                        similarity,
                        text
                    );


                // Store result
                localStorage.setItem(
                    "plagiarismResult",
                    JSON.stringify(result)
                );


                // Save history
                saveReport(result);


                // Go to result page
                setTimeout(function () {

                    window.location.href =
                        "result.html";

                }, 500);

            }

        }, 100);

}


// ==========================================
// SIMILARITY CALCULATION
// ==========================================

function calculateSimilarity(text) {

    if (!text || text.length === 0) {

        return 0;

    }


    /*
        Demo calculation.

        This is only for frontend testing.
        Later the Spring Boot backend will
        perform the real Java similarity
        calculation.
    */

    const words =
        text.toLowerCase()
            .replace(/[^\w\s]/g, "")
            .split(/\s+/)
            .filter(Boolean);


    if (words.length < 5) {

        return 5;

    }


    // Repeated words indicate possible similarity
    const frequency = {};

    words.forEach(function (word) {

        frequency[word] =
            (frequency[word] || 0) + 1;

    });


    let repeatedWords = 0;

    Object.keys(frequency).forEach(function (word) {

        if (frequency[word] > 1) {

            repeatedWords +=
                frequency[word] - 1;

        }

    });


    let similarity =
        (repeatedWords / words.length) * 100;


    // Keep demo result between 5 and 95
    similarity =
        Math.max(5, Math.min(95, similarity));


    return Number(
        similarity.toFixed(1)
    );

}


// ==========================================
// CREATE RESULT
// ==========================================

function createResult(
    documentName,
    similarity,
    text
) {

    let level = "Low";

    if (similarity >= 60) {

        level = "High";

    } else if (similarity >= 30) {

        level = "Medium";

    }


    return {

        documentName:
            documentName,

        similarity:
            similarity,

        original:
            Number(
                (100 - similarity).toFixed(1)
            ),

        level:
            level,

        date:
            new Date().toLocaleString(),

        matchingText:
            generateMatchingText(text)

    };

}


// ==========================================
// GENERATE MATCHING TEXT
// ==========================================

function generateMatchingText(text) {

    if (!text) {

        return "No matching text found.";

    }


    const sentences =
        text.split(/[.!?]+/)
            .map(function (sentence) {

                return sentence.trim();

            })
            .filter(function (sentence) {

                return sentence.length > 20;

            });


    if (sentences.length === 0) {

        return "No matching text found.";

    }


    // Display first possible matching sentence
    return sentences
        .slice(0, 2)
        .join(". ");
}


// ==========================================
// SAVE REPORT
// ==========================================

function saveReport(result) {

    let reports =
        JSON.parse(
            localStorage.getItem("reports")
        ) || [];


    reports.unshift(result);


    // Keep latest 50 reports
    reports =
        reports.slice(0, 50);


    localStorage.setItem(
        "reports",
        JSON.stringify(reports)
    );

}


// ==========================================
// LOAD RESULT
// ==========================================

function loadResult() {

    const stored =
        localStorage.getItem(
            "plagiarismResult"
        );


    if (!stored) {

        return;

    }


    const result =
        JSON.parse(stored);


    const documentName =
        document.getElementById(
            "resultDocumentName"
        );

    const date =
        document.getElementById(
            "resultDate"
        );

    const similarity =
        document.getElementById(
            "similarityPercentage"
        );

    const original =
        document.getElementById(
            "originalPercentage"
        );

    const level =
        document.getElementById(
            "plagiarismLevel"
        );

    const progress =
        document.getElementById(
            "similarityProgress"
        );

    const matchingText =
        document.getElementById(
            "matchingText"
        );


    if (documentName) {

        documentName.textContent =
            result.documentName;

    }


    if (date) {

        date.textContent =
            result.date;

    }


    if (similarity) {

        similarity.textContent =
            result.similarity + "%";

    }


    if (original) {

        original.textContent =
            result.original + "%";

    }


    if (level) {

        level.textContent =
            result.level;


        level.className =
            "badge " +
            getLevelClass(result.level);

    }


    if (progress) {

        progress.style.width =
            result.similarity + "%";

        progress.textContent =
            result.similarity + "%";

        progress.className =
            "progress-bar " +
            getLevelClass(result.level);

    }


    if (matchingText) {

        matchingText.innerHTML =
            "<p class='mb-0'>" +
            escapeHTML(result.matchingText) +
            "</p>";

    }

}


// ==========================================
// LEVEL COLOR
// ==========================================

function getLevelClass(level) {

    if (level === "High") {

        return "bg-danger";

    }

    if (level === "Medium") {

        return "bg-warning text-dark";

    }

    return "bg-success";

}


// ==========================================
// LOAD DASHBOARD
// ==========================================

function loadDashboard() {

    const reportsElement =
        document.getElementById(
            "recentReports"
        );


    if (!reportsElement) {

        return;

    }


    const reports =
        JSON.parse(
            localStorage.getItem("reports")
        ) || [];


    // Total checks
    const totalChecks =
        document.getElementById(
            "totalChecks"
        );

    if (totalChecks) {

        totalChecks.textContent =
            reports.length;

    }


    // Average similarity
    const averageElement =
        document.getElementById(
            "averageSimilarity"
        );


    let average = 0;

    if (reports.length > 0) {

        const total =
            reports.reduce(
                function (sum, report) {

                    return sum +
                        Number(report.similarity || 0);

                },
                0
            );

        average =
            total / reports.length;

    }


    if (averageElement) {

        averageElement.textContent =
            average.toFixed(1) + "%";

    }


    // High plagiarism
    const highCount =
        reports.filter(
            function (report) {

                return report.level === "High";

            }
        ).length;


    const highElement =
        document.getElementById(
            "highPlagiarism"
        );

    if (highElement) {

        highElement.textContent =
            highCount;

    }


    // Low plagiarism
    const lowCount =
        reports.filter(
            function (report) {

                return report.level === "Low";

            }
        ).length;


    const lowElement =
        document.getElementById(
            "lowPlagiarism"
        );

    if (lowElement) {

        lowElement.textContent =
            lowCount;

    }


    // Recent reports
    if (reports.length === 0) {

        reportsElement.innerHTML = `
            <tr>
                <td colspan="6"
                    class="text-center text-muted py-4">
                    No reports available.
                </td>
            </tr>
        `;

        return;

    }


    reportsElement.innerHTML =
        reports
            .slice(0, 10)
            .map(function (report, index) {

                return `
                    <tr>

                        <td>${index + 1}</td>

                        <td>
                            ${escapeHTML(
                                report.documentName
                            )}
                        </td>

                        <td>
                            <strong>
                                ${report.similarity}%
                            </strong>
                        </td>

                        <td>
                            <span class="badge ${
                                getLevelClass(
                                    report.level
                                )
                            }">
                                ${report.level}
                            </span>
                        </td>

                        <td>
                            ${escapeHTML(
                                report.date
                            )}
                        </td>

                        <td>

                            <button
                                class="btn btn-sm btn-outline-primary"
                                onclick="viewReport(${index})">

                                <i class="bi bi-eye"></i>

                            </button>

                        </td>

                    </tr>
                `;

            })
            .join("");

}


// ==========================================
// VIEW REPORT
// ==========================================

function viewReport(index) {

    const reports =
        JSON.parse(
            localStorage.getItem("reports")
        ) || [];


    if (!reports[index]) {

        return;

    }


    localStorage.setItem(
        "plagiarismResult",
        JSON.stringify(reports[index])
    );


    window.location.href =
        "result.html";

}


// ==========================================
// CLEAR UPLOAD FORM
// ==========================================

function clearForm() {

    const text =
        document.getElementById(
            "plagiarismText"
        );

    const file =
        document.getElementById(
            "documentFile"
        );

    const name =
        document.getElementById(
            "documentName"
        );

    const message =
        document.getElementById(
            "uploadMessage"
        );


    if (text) {

        text.value = "";

    }


    if (file) {

        file.value = "";

    }


    if (name) {

        name.value = "";

    }


    if (message) {

        message.className =
            "alert d-none";

        message.textContent = "";

    }

}


// ==========================================
// DOWNLOAD REPORT
// ==========================================

function downloadReport() {

    const stored =
        localStorage.getItem(
            "plagiarismResult"
        );


    if (!stored) {

        alert("No report available.");

        return;

    }


    const result =
        JSON.parse(stored);


    const content = `
PLAGIARISM DETECTION REPORT
============================

Document Name:
${result.documentName}

Checked On:
${result.date}

Similarity Percentage:
${result.similarity}%

Original Content:
${result.original}%

Plagiarism Level:
${result.level}

Matching Text:
${result.matchingText}

============================
Plagiarism Detection System
`;


    const blob =
        new Blob(
            [content],
            { type: "text/plain" }
        );


    const url =
        URL.createObjectURL(blob);


    const link =
        document.createElement("a");


    link.href = url;

    link.download =
        "plagiarism-report.txt";


    document.body.appendChild(link);

    link.click();

    document.body.removeChild(link);


    URL.revokeObjectURL(url);

}


// ==========================================
// SHOW HISTORY
// ==========================================

function showHistory() {

    const reports =
        JSON.parse(
            localStorage.getItem("reports")
        ) || [];


    if (reports.length === 0) {

        alert("No plagiarism reports available.");

        return;

    }


    let message =
        "PLAGIARISM CHECK HISTORY\n\n";


    reports.forEach(
        function (report, index) {

            message +=
                (index + 1) +
                ". " +
                report.documentName +
                " - " +
                report.similarity +
                "% - " +
                report.level +
                "\n";

        }
    );


    alert(message);

}


// ==========================================
// SHOW ANALYTICS
// ==========================================

function showAnalytics() {

    const reports =
        JSON.parse(
            localStorage.getItem("reports")
        ) || [];


    if (reports.length === 0) {

        alert("No data available for analytics.");

        return;

    }


    const high =
        reports.filter(
            r => r.level === "High"
        ).length;


    const medium =
        reports.filter(
            r => r.level === "Medium"
        ).length;


    const low =
        reports.filter(
            r => r.level === "Low"
        ).length;


    alert(
        "PLAGIARISM ANALYTICS\n\n" +
        "Total Checks: " +
        reports.length +
        "\n\nHigh: " +
        high +
        "\nMedium: " +
        medium +
        "\nLow: " +
        low
    );

}


// ==========================================
// SHOW MESSAGE
// ==========================================

function showMessage(
    element,
    text,
    type
) {

    if (!element) {

        return;

    }


    element.className =
        "alert alert-" + type + " mt-3";

    element.textContent =
        text;

}


// ==========================================
// ESCAPE HTML
// ==========================================

function escapeHTML(text) {

    if (!text) {

        return "";

    }


    return String(text)
        .replace(/&/g, "&amp;")
        .replace(/</g, "&lt;")
        .replace(/>/g, "&gt;")
        .replace(/"/g, "&quot;")
        .replace(/'/g, "&#039;");

}