package org.littil.api.auth.service;

import jakarta.annotation.PostConstruct;
import org.passay.CharacterData;
import org.passay.CharacterRule;
import org.passay.EnglishCharacterData;
import org.passay.PasswordGenerator;
import org.passay.PasswordValidator;

import jakarta.enterprise.inject.Produces;
import jakarta.inject.Singleton;
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
        lowerCaseRule.setNumberOfCharacters(3);
        rules.add(lowerCaseRule);

        CharacterData upperCaseChars = EnglishCharacterData.UpperCase;
        CharacterRule upperCaseRule = new CharacterRule(upperCaseChars);
        upperCaseRule.setNumberOfCharacters(3);
        rules.add(upperCaseRule);

        CharacterData digitChars = EnglishCharacterData.Digit;
        CharacterRule digitRule = new CharacterRule(digitChars);
        digitRule.setNumberOfCharacters(3);
        rules.add(digitRule);

    }

    @Produces
    public PasswordValidator validator() {
        return new PasswordValidator(rules);
    }

    @Produces
    public String generate() {
        PasswordGenerator pwdGenerator = new PasswordGenerator();
        var password =  pwdGenerator.generatePassword(12, rules);
        return new StringBuilder(password).insert(6, "-").toString();
    }
}
