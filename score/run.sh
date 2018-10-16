name=$1

DATA=score/data
SCTK=score/sctk-2.4.10
REF=${DATA}/${name}/ref.trn
HYP=${DATA}/${name}/hypo.trn

echo "statring evaluation" >> score.log 

if [ ! -f $REF ]; then
 echo "missing ref file" >> score.log 
 exit 1
fi

if [ ! -f $HYP ]; then
 echo "missing hypo file" >> score.log 
  exit 1
fi

echo $(pwd) >> score.log
echo $SCTK/bin/sclite -r $REF -h $HYP -i rm -o all >> score.log
$SCTK/bin/sclite -r $REF -h $HYP -i rm -o all
