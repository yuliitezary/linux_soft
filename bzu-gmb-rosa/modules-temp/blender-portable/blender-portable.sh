#!/bin/bash
#creator by RedRoot(Yaciyna Mikhail) for GAMER STATION [on linux] and Gaming Community OS Linux
# GPL-3.0 License

#проверяем что модуль запущен от пользователя root
#[ "$UID" -eq 0 ] || { zenity --error --text="Этот скрипт нужно запускать из под root!"; exit 1;}

# определение имени файла, папки где находиться скрипт и версию скрипта
name_script0=`basename "$0"`
name_script=`echo ${name_script0} | sed 's/\.sh\>//g'`
script_dir0=$(cd $(dirname "$0") && pwd); name_cut="/modules-temp/${name_script}"
script_dir=`echo ${script_dir0} | sed "s|${name_cut}||g"`
version0=`cat "${script_dir}/config/name_version"`
version="${version0}"
user_run_script=`cat "${script_dir}/config/user"`
user_app_dir=`cat "${script_dir}/config/app-dir"`
user_utils_dir=`cat "${script_dir}/config/utils-dir"`
name_app_folder="blender-portable"
name_app_arhive="blender-portable-3-4.tar.xz"
name_script_start="blender-portable-starter.sh"
name_pack_apps="v1"
#Определение расположениея папок для утилит и т.д.
#utils_dir="${script_dir}/core-utils"

#Определение переменныех утилит и скриптов
#YAD="${utils_dir}/yad"
#zenity="${utils_dir}/zenity"

#объявляем нужные переменные для скрипта
date_install=`date`
#загружаем данные о модули и файла конфигурации в массив
readarray -t module_conf < "${script_dir}/modules-temp/${name_script}/module_config"
#примеры считывания массива с данными
#version_kernel=${module_conf[*]} - Все записи в массиве
#version_kernel=${#module_conf[*]} - Количество записей в массиве, нумерания с нуля
#version_kernel=${module_conf[7]} - Определенная запись в массиве
version_app=${module_conf[7]}
#получение пароля root пользователя
pass_user="$1"

#даем информацию в терминал какой модуль устанавливается
tput setaf 2; echo "Установка Blender-portable - программное обеспечение для создания трёхмерной компьютерной графики [https://www.blender.org/]. Установка Blender осуществлыется в формате Portable. Версия скрипта 1.0b, автор: Яцына М.А."
tput sgr0


#запуск основных команд модуля
# Проверка что существует папка applications, если нет, создаем ее
if [ ! -d "/home/${user_run_script}/.local/share/applications" ]
then
mkdir -p "/home/${user_run_script}/.local/share/applications"
fi

# Проверка что существует папка .local/bin, если нет, создаем ее
#if [ ! -d "/home/${user_run_script}/.local/bin" ]
#then
#mkdir -p "/home/${user_run_script}/.local/bin"
#fi

# Проверка установлен Видеоредактор или нет в папке пользователя
if [ -d "/home/${user_run_script}/${user_app_dir}/${name_app_folder}" ];then

            install_name_app_arhive=`cat /home/${user_run_script}/${user_app_dir}/${name_app_folder}/app_arhive`

                if [ ${install_name_app_arhive} = ${name_app_arhive} ]; then
                    tput setaf 1; echo "Данная версия программы ${name_script} уже установлена в папку пользователя ${user_run_script}, что бы не стереть ваши важные данные, установка прирывается!"
                    tput sgr0
                    else 
                    tput setaf 2; echo "Устанавливается программа ${name_script} более новой версии в папку пользователя ${user_run_script}"
                    tput sgr0
                    app_installing="true"
                fi

        else
                    tput setaf 2; echo "Устанавливается программа ${name_script} в папку пользователя ${user_run_script}"
                    tput sgr0
                    app_installing="true"
fi

if [[ $app_installing == "true" ]];then
#tput setaf 2; echo "Начинается установка редактора визуальных эффектовУтилита ${name_script}  в папку пользователя ${user_run_script}"
#put sgr0
cd "/home/${user_run_script}/${user_app_dir}"
rm -f ${name_app_arhive} || true
rm -fr ${name_app_arhive} || true
wget "https://github.com/redrootmin/bzu-gmb-modules/releases/download/${name_pack_apps}/${name_app_arhive}"
pv "${name_app_arhive}" | tar -xJ
rm -f ${name_app_arhive}
cd "/home/${user_run_script}/${user_app_dir}/${name_app_folder}";chmod +x mini_install.sh
bash mini_install.sh

# 5 секунд теста программы
#app_name="Natron"
#echo "Testing:${version_app}"
#cd "/home/${user_run_script}/${name_app_folder}"
#echo "Папка установки:/home/${user_run_script}/${name_app_folder}"
#bash -c "/home/${user_run_script}/${name_app_folder}/${name_script_start}" & sleep 5;echo "${pass_user}" | sudo -S killall "${app_name}"
tput setaf 2; echo "Установка программы ${name_script} завершена :)"
tput sgr0
fi



#добавляем информацию в лог установки о уровне ошибок модуля, чем выше цифра, тем больше было ошибок и нужно проверить модуль разработчику
echo "модуль ${name_script}, дата установки:${date_install}, количество ошибок:${error}"	 				  >> "${script_dir}/module_install_log"

exit 0


#Для создания скрипта использовались следующие ссылки
#https://techblog.sdstudio.top/blog/google-drive-vstavliaem-priamuiu-ssylku-na-izobrazhenie-sayta
#https://www.linuxliteos.com/forums/scripting-and-bash/preview-and-download-images-and-files-with-zenity-dialog/
#https://www.ibm.com/developerworks/ru/library/l-zenity/
#https://habr.com/ru/post/281034/
#https://webhamster.ru/mytetrashare/index/mtb0/20
#https://itfb.com.ua/kak-prisvoit-rezultat-komandy-peremennoj-obolochki/
#https://tproger.ru/translations/bash-cheatsheet/
#https://mirivlad.ru/2017/11/20-primerov-ispolzovaniya-potokovogo-tekstovogo-redaktora-sed/
#https://www.opennet.ru/docs/RUS/bash_scripting_guide/c1833.html
#https://losst.ru/massivy-bash
#https://www.shellhacks.com/ru/grep-or-grep-and-grep-not-match-multiple-patterns/
#https://techrocks.ru/2019/01/21/bash-if-statements-tips/
#https://habr.com/ru/post/511608/
#https://techrocks.ru/2019/01/21/bash-if-statements-tips/
