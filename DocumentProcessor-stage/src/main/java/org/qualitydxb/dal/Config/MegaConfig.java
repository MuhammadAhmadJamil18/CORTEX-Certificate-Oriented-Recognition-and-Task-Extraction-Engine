// package org.qualitydxb.dal.Config;


// import io.github.eliux.mega.MegaSession;
// import org.springframework.context.annotation.Bean;
// import org.springframework.context.annotation.Configuration;
// import io.github.eliux.mega.cmd.MegaCmdLogin;

// @Configuration
// public class MegaConfig {

//     @Bean
//     public MegaSession megaSession() {
//         // Provide empty credentials. This implements MegaAuth internally,
//         // but won't do anything unless you explicitly run the login command.
//         // MegaAuth noCredentials = new MegaCmdLogin("", "");

//         // Construct MegaSession with that "dummy" auth
//         // return new MegaSession(noCredentials);
//         return new MegaSession(new MegaCmdLogin("", ""));
//     }
// }

