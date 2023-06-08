package ru.vsu.cs.proskuryakov.mas.ui.login;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import android.content.Context;
import android.util.Patterns;

import java.io.FileNotFoundException;

import ru.vsu.cs.proskuryakov.mas.data.LoginRepository;
import ru.vsu.cs.proskuryakov.mas.data.Result;
import ru.vsu.cs.proskuryakov.mas.data.model.CustomData;
import ru.vsu.cs.proskuryakov.mas.data.model.LoggedInUser;
import ru.vsu.cs.proskuryakov.mas.R;

public class LoginViewModel extends ViewModel {

    private MutableLiveData<LoginFormState> loginFormState = new MutableLiveData<>();
    private MutableLiveData<LoginResult> loginResult = new MutableLiveData<>();
    private LoginRepository loginRepository;

    LoginViewModel(LoginRepository loginRepository) {
        this.loginRepository = loginRepository;
    }

    LiveData<LoginFormState> getLoginFormState() {
        return loginFormState;
    }

    LiveData<LoginResult> getLoginResult() {
        return loginResult;
    }

    public void login(String username, String password, Context context) throws FileNotFoundException {
        Result result = loginRepository.login(username, password, context);
        // В случае успешного входа
        if (result instanceof Result.Success) {
            LoggedInUser data = ((Result.Success<LoggedInUser>) result).getData();
            // Получаем дополнительные данные пользователя (например, число)
            CustomData customData = new CustomData("custom data");
            // Формируем данные в структуру, для передачи в главное окно
            LoggedInUserView userData = new LoggedInUserView(data.getDisplayName(), customData);
            loginResult.setValue(new LoginResult(userData));
        } else if (result instanceof Result.Error) {
            // В случае неудачного входа передаем сообщение об ошибке
            loginResult.setValue(new LoginResult(((Result.Error)result).getError()));
        }
    }

    public void loginDataChanged(String username, String password) {
        if (!isUserNameValid(username)) {
            loginFormState.setValue(new LoginFormState(R.string.invalid_username, null));
        } else if (!isPasswordValid(password)) {
            loginFormState.setValue(new LoginFormState(null, R.string.invalid_password));
        } else {
            loginFormState.setValue(new LoginFormState(true));
        }
    }

    // A placeholder username validation check
    private boolean isUserNameValid(String username) {
        if (username == null) {
            return false;
        }
        if (username.contains("@")) {
            return Patterns.EMAIL_ADDRESS.matcher(username).matches();
        } else {
            return !username.trim().isEmpty();
        }
    }

    // A placeholder password validation check
    private boolean isPasswordValid(String password) {
        return password != null && password.trim().length() > 5;
    }

    public void logout() {
        loginRepository.logout();
    }

}