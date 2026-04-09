packages="com.android.packageinstaller"

check_support() {
    android_ver=$(getprop ro.build.version.release)
    ui_print "- Checking support..."
    if [ "$android_ver" -lt 16 ]; then
        abort "- Your ColorOS Version is not supported"
    fi
}

uninstall_updates() {
    ui_print "- Uninstalling installer updates..."
    for pkg in $packages; do
        pm uninstall-system-updates $pkg >/dev/null 2>&1
    done
}

get_package_info() {
    pkg=$1
    installed=$(pm list packages | grep -q $pkg && echo true || echo false)
    path=$(pm path $pkg | sed 's/package://')
    folder=$(dirname "$path")
}

install_package() {
    installer=$1
    partition=$2
    replace_folder=$3

    ui_print "- Replacing with $installer"

    mkdir -p "$MODPATH$replace_folder"
    cp -rf "$MODPATH/${installer}.apk" "$MODPATH$replace_folder"

    REPLACE="
    $replace_folder
    "
}

add_installer() {
    ui_print "- Installing files for Android $android_ver"

    uninstall_updates

    for pkg in $packages; do
        get_package_info "$pkg"
        if [ "$installed" = "true" ]; then
            if [[ "$path" == *product* ]]; then
                partition=/system/product
                replace_folder="/system$folder"
            elif [[ "$path" == *system_ext* ]]; then
                partition=/system/system_ext
                replace_folder="/system$folder"
            elif [[ "$path" == *vendor* ]]; then
                partition=/system/vendor
                replace_folder="/system$folder"
            elif [[ "$path" == *system* ]]; then
                partition=/system
                replace_folder="$folder"
            fi

            case $pkg in
                com.android.packageinstaller) install_package "PackageInstaller" "$partition" "$replace_folder" ;;
            esac
            return
        fi
    done

    abort "- No supported package installer found"
}

clean_files() {
    rm -rf "$MODPATH/PackageInstaller.apk" 2>/dev/null
    rm -f "$MODPATH/install.sh" 2>/dev/null
    rm -rf /data/resource-cache/* /data/system/package_cache/* 2>/dev/null
}

run_install() {
    mods_center
    check_support
    add_installer
    clean_files
}

run_install