# WeatherApp

Алышев Никита

## Задание

Написать приложение Погода.
Для загрузки погоды используйте:
http://openweathermap.org/api/hourly-forecast
### Функционал приложения:
1. При первом входе пользователь должен выбрать
свой город
2. На основном экране должна показываться
детальная информация о погоде в городе с момента
открытия приложения на ближайшие
сутки (24 часа)
3. Реализовать просмотр погоды на последующие дни.
Например, на
предстоящую неделю.
4. Реализовать возможность добавлять
дополнительные города
5. Реализовать навигацию между экранами городов

## Описание проекта
Приложение предоставляет информацию о погоде в выбранных городах. При первом входе пользователь выбирает свой город, и приложение отображает информацию о погоде в этом городе.

Пользователи также могут просматривать прогноз погоды на последующие 5 дней и на ближайшие 24 часа с интервалом в 3 часа. Кроме того, приложение предлагает возможность добавлять дополнительные города для отслеживания погоды в них.

При вводе города проверяется существует ли такой город. Для этого в программе есть документ со всеми городами на местном и Русском языке.
Для отображения погоды использовалось 2 API. "Current Weather Data" отображает текущую погоду, а "5 Day / 3 Hour Forecast" отображает прогноз на 1 и 5 дней.
Прогноз погоды дается по местному, для выбранного города, времени.
Выбранный город сохраняетя даже после перезапуска приложения.
Добавлена возможность смены темы на темную, по умолчанию стоит светлая.


Для запуска проекта можно скачать apk-файл, который доступен на GitHub, или скачать весь проект и собрать его в Android Studio.