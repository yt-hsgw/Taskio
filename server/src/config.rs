use dotenvy::dotenv;
use std::env;

pub fn get_port() -> u16 {
    dotenv().ok();
    env::var("PORT")
        .unwrap_or_else(|_| "3000".to_string())
        .parse()
        .expect("PORT must be a number")
}   