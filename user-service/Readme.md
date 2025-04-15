mutation {
login(username: "testuser", password: "password") {
token
}
}

mutation CreateNewUser($userInput: CreateUserRequest!) {
createUser(input: $userInput) {
id
userId
username
}
}
{
"userInput": {
"userId": "customer456",
"username": "anotheruser",
"password": "anotherSecurePassword456"
}
}

{
"userId": "customer123",
"username": "testuser",
"password": "verySecurePassword123"
}
