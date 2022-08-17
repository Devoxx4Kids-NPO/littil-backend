package org.littil.api.auth.service;

import org.passay.CharacterData;
import org.passay.CharacterRule;
import org.passay.EnglishCharacterData;
import org.passay.PasswordGenerator;
import org.passay.PasswordValidator;

import javax.annotation.PostConstruct;
import javax.enterprise.inject.Produces;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.List;

import static org.passay.IllegalCharacterRule.ERROR_CODE;

@Singleton
public class PasswordService {

    private List<CharacterRule> rules;

    @PostConstruct
    public void init() {
        rules = new ArrayList<>();
        CharacterData lowerCaseChars = EnglishCharacterData.LowerCase;
        CharacterRule lowerCaseRule = new CharacterRule(lowerCaseChars);
        lowerCaseRule.setNumberOfCharacters(2);
        rules.add(lowerCaseRule);

        CharacterData upperCaseChars = EnglishCharacterData.UpperCase;
        CharacterRule upperCaseRule = new CharacterRule(upperCaseChars);
        upperCaseRule.setNumberOfCharacters(2);
        rules.add(upperCaseRule);

        CharacterData digitChars = EnglishCharacterData.Digit;
        CharacterRule digitRule = new CharacterRule(digitChars);
        digitRule.setNumberOfCharacters(2);
        rules.add(digitRule);

        CharacterData specialChars = new CharacterData() {
            public String getErrorCode() {
                return ERROR_CODE;
            }

            public String getCharacters() {
                return "!@#$%^*()_+";
            }
        };
        CharacterRule splCharRule = new CharacterRule(specialChars);
        splCharRule.setNumberOfCharacters(2);
        rules.add(splCharRule);
    }

    @Produces
    public PasswordValidator validator() {
        return new PasswordValidator(rules);
    }

    @Produces
    public String generate() {
        PasswordGenerator pwdGenerator = new PasswordGenerator();
        return pwdGenerator.generatePassword(10, rules);
    }
}
