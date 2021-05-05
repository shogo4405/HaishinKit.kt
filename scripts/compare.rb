if ARGV.size() == 0 then
  die
else
  main = ARGV[0].to_s
end

Dir.chdir(main)
for num in 0..1000000 do
  file = main + "/" + sprintf("v-%010d.bmp", num)
  puts file
end


