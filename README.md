# SurvivalBoom Discord Service
**SBDS** - універсальна платформа для розробки Discord ботів.
*Це просто диво!*

## API
Реально зручне API та абстракції над складними задачами.
- Commands (String, Console, Slash, Context)
- Modal (Централізована система надсилання Modal із шаблонізацією)
- InteractableComponents (Централізована система обробки взаємодій із кнопками)
- Events (Включаючи створення власних)
- Database (Модульна система репозиторіїв, вбудовані сховища для даних користувачів та серверів, підтримує будь-які способи зберігання даних)
- GuildConfig (Універсальне централізоване сховище налаштувань модулів для конкретного серверу)
- Libraries (Ізолювання кожної версії бібліотеки та модулів, динамічне завантаження залежностей модулів)
- Translations (Повна підтримка перекладів, налаштування через YAML, у тому числі для кожного модуля окремо)
- Permissions (Універсальна система дозволів та груп, глобальні, серверні та для учасників)
- Modules (Динамічно підвантажувані модулі, що використовують SBDS API)
- RegistrationRegistry (Усі реєстрації під контролем одного централізованого сховища)
- ServiceProvider (Реєстрація API модулів)

### Приклади

Треба швиденько зробити команду? Ось!
```java
@CommandClass(name = "ban", description = "Ban user", permission = "mymodule.command.ban")
public class MyBanCommand extends CommandBase implements StringCommandExecutor, SlashCommandExecutor, ConsoleCommandExecutor {
    
    @Override
    public void executes(@NotNull SlashExecutionInfo info) {
        executes0(info);
    }
    
    @Override
    public void executes(@NotNull StringExecutionInfo info) {
        executes0(info);
    }
    
    private void executes0(@NotNull InteractionHolder info) {
        
        User user = info.arguments().getCast("user", User.class).orElseThrow();
        String reason = info.arguments().getCast("reason", String.class).orElseThrow();
        
        Guild guild = info.guild();
        Member member = guild.retrieveMember(user).complete();
        
        member.ban(reason).queue();
        
        info.reply("mymodule.command.ban.success")
                .withPlaceholders("user", member)
                .queue();
        
    }
    
    @Override
    public void executes(@NotNull ConsoleExecutionInfo info) {

        User user = info.arguments().getCast("user", User.class).orElseThrow();
        Guild guild = info.arguments().getCast("guild", Guild.class).orElseThrow();
        String reason = info.arguments().getCast("reason", String.class).orElseThrow();

        Member member = guild.retrieveMember(user).complete();
        member.ban(reason).queue();
        
        info.logger().info("Banned `{}` successfully!", member.getEffectiveName());
        
    }
    
    @ArgumentMethod(scope = ArgumentScope.CONSOLE)
    public GuildArgument guild() {
        return new GuildArgument();
    }
    
    @ArgumentMethod(index = 1)
    public UserArgument user() {
        return new UserArgument();
    }
    
    @ArgumentMethod(index = 2)
    public StringArgument reason() {
        return new StringArgument();
    }
    
}

```
І магічним чином тепер існує команда /ban, або ж !ban та варіант для консолі. При виконанні команди автоматично перевіряється її Permission.

А потім:
```java
public class MyModule extends ModuleMain {
    
    @Override
    public void onEnable() {
        registerCommand(new MyBanCommand());
    }
    
}
```

## Модулі

Коли проєкт розростається все більше і більше, код перетворюється у величезну тарілку заплутанного спагетті. У наслідок чого вирішення проблеми та додавання нового функціоналу все більше схоже на катування.
Саме тому SBDS має **модульну систему**.

Кожен модуль - окремий проєкт, що фокусується на конкретній задачі та використовує SBDS API для виконання задачі.
При запуску SBDS виконує усю брудну роботу: завантажує бібліотеки, підключається до бази даних, ініціалізує усі менеджери, розгортає усе API. 
Після цього модулі завантажуються і запускаються. Модулі можуть бути динамічно завантажені чи вивантажені під час роботи бота.
У процесі роботи модулі викликають API з SBDS, що виконує усі підкапотні операції, тим самим спрощуючи життя. Під час розробки модуля ви концентруєтесь на функціоналі, а не на тому як надсилати запити у базу даних.

Цей репозиторій включає деякі модулі написані нами, що покривають загальні потреби та служать як приклад використання API.

## Збірка
💥 Вам необхідна Java >= 21 та gradle.

Щоб розпочати, завантажте репозиторій `git clone https://github.com/SurvivalBoom/SurvivalBoomDiscordService.git`

**Збірка ядра**
* Виконайте `gradle build` щоб зібрати ядро в виконуваний JAR.
* Ви можете запустити SBDS прямо з проєкту використавши `gradle runApp`

**Підготовка середовища розробки**
1. Виконайте `gradle publishToMavenLocal`
2. Створіть новий проєкт та додайте у ваш **build.gradle.kts**:
```kotlin
repositories {
    mavenLocal()
}

dependencies {
    compileOnly("net.survivalboom.sbds:api:{version}")
}
```
3. Насолоджуйтесь!

### SurvivalBoom Network 2026 | Ми - найкращі! | Слава Україні!
