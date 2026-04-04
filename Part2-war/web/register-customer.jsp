<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<!DOCTYPE html>
<html>
<head>
    <title>Customer Registration - EPDA</title>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <link href="${pageContext.request.contextPath}/resources/css/style.css" rel="stylesheet" type="text/css"/>
    <style>
        .registration-container { max-width: 600px; margin: 50px auto; padding: 20px; background: #f9f9f9; border-radius: 8px; }
        .form-group { margin-bottom: 15px; }
        label { display: block; margin-bottom: 5px; font-weight: bold; }
        input[type="text"], input[type="email"], input[type="password"], select, textarea {
            width: 100%; padding: 10px; border: 1px solid #ddd; border-radius: 4px; font-size: 14px;
        }
        input[type="text"]:focus, input[type="email"]:focus, input[type="password"]:focus, select:focus, textarea:focus {
            outline: none; border-color: #007bff; box-shadow: 0 0 5px rgba(0,123,255,0.3);
        }
        .error { color: #d32f2f; font-size: 12px; margin-top: 5px; }
        .success { color: #388e3c; font-size: 12px; margin-top: 5px; }
        .button-group { display: flex; gap: 10px; margin-top: 20px; }
        button { flex: 1; padding: 12px; border: none; border-radius: 4px; font-size: 16px; cursor: pointer; }
        .btn-register { background: #007bff; color: white; }
        .btn-register:hover { background: #0056b3; }
        .btn-cancel { background: #ccc; color: #333; }
        .btn-cancel:hover { background: #bbb; }
        .form-title { text-align: center; margin-bottom: 30px; }
        .form-hint { font-size: 12px; color: #666; margin-top: 3px; }
    </style>
</head>
<body>
    <div class="registration-container">
        <div class="form-title">
            <h2>Customer Registration</h2>
            <p>Complete all fields below to create your account</p>
        </div>

        <form id="registrationForm" method="POST" onsubmit="submitForm(event)">
            <input type="hidden" name="userType" value="CUSTOMER" />

            <div class="form-group">
                <label for="name">Full Name <span style="color: red;">*</span></label>
                <input type="text" id="name" name="name" required placeholder="John Doe" />
                <div class="form-hint">Letters and spaces only (2-100 characters)</div>
                <div class="error" id="nameError"></div>
            </div>

            <div class="form-group">
                <label for="email">Email <span style="color: red;">*</span></label>
                <input type="email" id="email" name="email" required placeholder="john@example.com" />
                <div class="form-hint">example@domain.com</div>
                <div class="error" id="emailError"></div>
            </div>

            <div class="form-group">
                <label for="password">Password <span style="color: red;">*</span></label>
                <input type="password" id="password" name="password" required placeholder="••••••••" />
                <div class="form-hint">At least 8 chars: uppercase, lowercase, digit, special char (!@#$%^&*)</div>
                <div class="error" id="passwordError"></div>
            </div>

            <div class="form-group">
                <label for="passwordConfirm">Confirm Password <span style="color: red;">*</span></label>
                <input type="password" id="passwordConfirm" name="passwordConfirm" required placeholder="••••••••" />
                <div class="error" id="passwordConfirmError"></div>
            </div>

            <div class="form-group">
                <label for="gender">Gender <span style="color: red;">*</span></label>
                <select id="gender" name="gender" required>
                    <option value="">-- Select Gender --</option>
                    <option value="M">Male</option>
                    <option value="F">Female</option>
                    <option value="Other">Other</option>
                </select>
                <div class="error" id="genderError"></div>
            </div>

            <div class="form-group">
                <label for="phone">Phone Number <span style="color: red;">*</span></label>
                <input type="text" id="phone" name="phone" required placeholder="60123456789" />
                <div class="form-hint">7-15 digits, may start with +</div>
                <div class="error" id="phoneError"></div>
            </div>

            <div class="form-group">
                <label for="ic">IC/ID Number <span style="color: red;">*</span></label>
                <input type="text" id="ic" name="ic" required placeholder="123456-12-1234 or 123456121234" />
                <div class="form-hint">Malaysian format: 123456-12-1234 or 12-digit</div>
                <div class="error" id="icError"></div>
            </div>

            <div class="form-group">
                <label for="address">Address <span style="color: red;">*</span></label>
                <textarea id="address" name="address" required rows="3" placeholder="123 Main Street, City, State"></textarea>
                <div class="form-hint">5-255 characters</div>
                <div class="error" id="addressError"></div>
            </div>

            <div id="serverMessage"></div>

            <div class="button-group">
                <button type="submit" class="btn-register">Register</button>
                <button type="button" class="btn-cancel" onclick="window.location.href='${pageContext.request.contextPath}/login.xhtml'">Cancel</button>
            </div>
        </form>
    </div>

    <script>
        function submitForm(event) {
            event.preventDefault();

            // Clear previous errors
            document.querySelectorAll('.error').forEach(el => el.innerText = '');
            document.getElementById('serverMessage').innerHTML = '';

            const formData = new FormData(document.getElementById('registrationForm'));

            // Send to servlet
            fetch('<%= request.getContextPath() %>/register', {
                method: 'POST',
                body: formData
            })
            .then(response => response.json())
            .then(data => {
                if (data.success) {
                    document.getElementById('serverMessage').innerHTML = '<p style="color: #388e3c;"><strong>' + data.message + '</strong></p>';
                    setTimeout(() => {
                        window.location.href = data.redirectUrl;
                    }, 2000);
                } else {
                    document.getElementById('serverMessage').innerHTML = '<p style="color: #d32f2f;"><strong>' + data.message + '</strong></p>';
                }
            })
            .catch(error => {
                console.error('Error:', error);
                document.getElementById('serverMessage').innerHTML = '<p style="color: #d32f2f;"><strong>Registration failed. Please try again.</strong></p>';
            });
        }
    </script>
</body>
</html>
