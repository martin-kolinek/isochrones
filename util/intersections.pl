my $startx = 17.04;
my $countx = 10;
my $diffx = 0.01;
my $starty = 48.13;
my $county = 10;
my $diffy = 0.01;

for(my $i=0; $i<$countx; $i++) 
{
    for(my $j=0; $j<$county; $j++) 
    {
        $sx = $startx + $i*$diffx;
        $ex = $sx+$diffx;
        $sy = $starty + $j*$diffy;
        $ey = $sy+$diffy;
        print "psql -c \"insert into intersecting_pairs(s_node1, e_node1, s_node2, e_node2) select start1, end1, start2, end2 from st_getintersections($sx, $sy, $ex, $ey)\"\n";
        `psql -c "insert into intersecting_pairs(s_node1, e_node1, s_node2, e_node2) select start1, end1, start2, end2 from st_getintersections($sx, $sy, $ex, $ey)"`;
    }
}
    
