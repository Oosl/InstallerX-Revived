
tmp_list="framework-res"

dda=/data/dalvik-cache/arm
[ -d $dda"64" ] && dda=$dda"64"
for i in $tmp_list; do
	rm -f $dda/system@*@"$i"*
done
rm -rf /data/system/package_cache/*
